package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.cxs.config.AliPayConfig;
import com.cxs.domain.*;
import com.cxs.enums.OrderStatus;
import com.cxs.enums.PayStatus;
import com.cxs.enums.ShipStatus;
import com.cxs.mapper.OrderActionMapper;
import com.cxs.mapper.OrderGoodsMapper;
import com.cxs.mapper.OrderMapper;
import com.cxs.model.PageBean;
import com.cxs.model.SearchGoodsVo;
import com.cxs.redis.RedisClient;
import com.cxs.service.CartService;
import com.cxs.service.GoodsService;
import com.cxs.service.OrderService;
import com.cxs.utils.AuthCodeUtil;
import com.cxs.utils.DateUtil;
import com.cxs.vo.OrderVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/8 20:20
 */
@Service(retries = 1)
public class OrderServiceImpl implements OrderService, MessageListener {

    @Reference
    private CartService cartService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderGoodsMapper orderGoodsMapper;
    @Autowired
    private OrderActionMapper orderActionMapper;
    @Reference
    private GoodsService goodsService;

    @Autowired
    private RedisClient redis;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    @JmsListener(concurrency = "3-5", containerFactory = "containerFactory", destination = "pre.order.queue")
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        OrderVo orderVo = null;
        try {
            orderVo = (OrderVo) objectMessage.getObject();
            //生成订单
            createPreOrder(orderVo);
            message.acknowledge();
        } catch (Exception e) {
            e.printStackTrace();
            //出现失败了就尝试三次
            int i = 3;
            while (i > 0) {
                try {
                    //生成订单 成功就跳出
                    createPreOrder(orderVo);
                    break;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    i--;
                }
            }
        }
    }

    /**
     * 生成预订单 会有分布式事物的问题
     *
     * @param orderVo
     */
    @Transactional(rollbackFor = Exception.class)
    void createPreOrder(OrderVo orderVo) {
        //1清空购物车 用于存放商品id 和商品数量的集合
        Map<Integer, Integer> goodsInfo = new HashMap<>(16);
        //得到登录用户的id
        Short userId = orderVo.getUser().getId();
        List<SearchGoodsVo> goodsVoList = orderVo.getGoodsVoList();
        double totalAmount = 0.00;
        for (SearchGoodsVo goodsVo : goodsVoList) {
            goodsInfo.put(Integer.valueOf(goodsVo.getId()), goodsVo.getSize());
            totalAmount += goodsVo.getSize() * Double.valueOf(goodsVo.getGoodsPrice());
        }
        try {

            //远程调用购物车的服务 去根据用户id和商品id删除预下单的商品
            this.cartService.cleanCart(goodsInfo, userId.intValue());
            //库存--
            goodsService.decrementStock(goodsInfo);
            //获得订单的编号 写一个工具类common里面
            String orderSn = getOrderSn();
            //处理订单  写order表
            Integer orderId = handlerOrder(userId.intValue(), orderSn, totalAmount);
            if (orderId != null) {
                //说明订单表写成功了 就写order——goods表
                handlerOrderGoods(goodsVoList, orderId);
                handlerOrderAvtion(userId, orderId);
            }
            //将预支付的订单放在mq中 开启延时队列模式哦
            jmsTemplate.convertAndSend("pre.order", orderId + "", new MessagePostProcessor() {
                //延时队列
                @Override
                public Message postProcessMessage(Message message) throws JMSException {
                    message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 10 * 60 * 1000);
                    return message;
                }
            });
        } catch (Exception e) {
            //如果出现异常 手动补偿一下
            cartService.compensateCart(goodsInfo, userId.intValue());
            //库存补偿
            goodsService.incrementStock(goodsInfo);
            throw new RuntimeException("让本地事物回滚，如果这不抛出异常 会被自己写的try捕获 事物就无法捕获回滚本地的异常了");
        }
    }

    /**
     * 写用户和订单的行为表
     *
     * @param userId
     * @param orderId
     */
    private void handlerOrderAvtion(Short userId, Integer orderId) {
        OrderAction orderAction = new OrderAction();
        orderAction.setOrderId(orderId);
        orderAction.setActionUser(userId.intValue());
        orderAction.setPayStatus((byte) PayStatus.NOT_PAY.getCode());
        orderAction.setOrderStatus((byte) OrderStatus.PRE_ORDER.getCode());
        orderAction.setShippingStatus((byte) ShipStatus.NOT_SHIP.getCode());
        orderAction.setActionNote("增加了一个预订单");
        orderActionMapper.insert(orderAction);

    }

    /**
     * 写order_goods表
     *
     * @param goodsVoList
     * @param orderId
     */
    @Transactional(rollbackFor = Exception.class)
    void handlerOrderGoods(List<SearchGoodsVo> goodsVoList, Integer orderId) {
        goodsVoList.forEach((goodsVo) -> {
            OrderGoods orderGoods = new OrderGoods();
            orderGoods.setOrderId(orderId);
            orderGoods.setGoodsId(Integer.valueOf(goodsVo.getId()));
            orderGoods.setGoodsNum(goodsVo.getSize().shortValue());
            orderGoods.setGoodsName(goodsVo.getGoodsNameHl());
            orderGoods.setGoodsPrice(new BigDecimal(goodsVo.getGoodsPrice()));
            orderGoodsMapper.insert(orderGoods);
        });
    }

    /**
     * 写订单表
     *
     * @param userId
     * @param orderSn
     * @return
     */
    private Integer handlerOrder(int userId, String orderSn, double totalAmount) {
        Order order = new Order();
        order.setAddTime(Integer.valueOf(DateUtil.getWantedStr("yyMMddHHmm")));
        order.setOrderSn(orderSn);
        order.setUserId(userId);
        order.setOrderStatus((byte) OrderStatus.PRE_ORDER.getCode());
        order.setShippingStatus((byte) ShipStatus.NOT_SHIP.getCode());
        order.setPayStatus((byte) PayStatus.NOT_PAY.getCode());
        order.setTotalAmount(new BigDecimal(totalAmount));
        orderMapper.insertSelective(order);
        return order.getId();
    }

    /**
     * 生成订单编号的方法
     *
     * @return
     */
    private String getOrderSn() {
        String wantedStr = DateUtil.getWantedStr("yyyy-MM-dd");
        Long incre = redis.incre("EGO-ORDER" + wantedStr);
        String yyyyMMddHHmm = DateUtil.getWantedStr("yyyyMMddHHmm");
        //生成订单号
        String orderSn = yyyyMMddHHmm + AuthCodeUtil.createCode(4) + AuthCodeUtil.getIncreStr(incre + "", 4);
        return orderSn;
    }

    /**
     * 分页查询订单
     *
     * @param id
     * @param orderStatus
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageBean<Order> queryOrderByPage(Short id, OrderStatus orderStatus, Integer page, Integer rows) {
        OrderExample example = new OrderExample();
        OrderExample.Criteria criteria = example.createCriteria();
        example.setOrderByClause("add_time desc");
        criteria.andUserIdEqualTo(id.intValue());
        criteria.andOrderStatusEqualTo((byte) orderStatus.getCode());
        Page<Object> page1 = PageHelper.startPage(page, rows);
        List<Order> orders = orderMapper.selectByExample(example);
        return new PageBean<>(page1.getTotal(), orders, page, rows);
    }

    /**
     * 根据订单号 查询订单
     *
     * @param orderSn
     * @return
     */
    @Override
    public OrderVo loadOrderByOrderSn(String orderSn) {
        OrderVo orderVo = new OrderVo();
        //根据订单号 查询这个订单
        OrderExample example = new OrderExample();
        example.createCriteria().andOrderSnEqualTo(orderSn);
        List<Order> orders = this.orderMapper.selectByExample(example);
        if (orders == null || orders.size() == 0) {
            throw new RuntimeException("订单不存在");
        }
        Order order = orders.get(0);
        orderVo.setOutTradeNo(order.getOrderSn());
        orderVo.setTotalAmount(order.getTotalAmount().toString());
        orderVo.setOrderName("EGO-SHOP-ORDER");
        //在根据订单的id 查询order_goods表
        OrderGoodsExample og = new OrderGoodsExample();
        og.createCriteria().andOrderIdEqualTo(order.getId());
        List<OrderGoods> orderGoods = this.orderGoodsMapper.selectByExample(og);
        int total = 0;
        for (OrderGoods orderGood : orderGoods) {
            total += orderGood.getGoodsNum();
        }
        String goodsName = orderGoods.get(0).getGoodsName();
        orderVo.setOrderInfo(goodsName + "...等,共:" + total + "件商品");
        return orderVo;
    }

    /**
     * 检查数据库订单是否支付完成
     *
     * @param orderSn
     * @return
     */
    @Override
    public Order query(String orderSn) {
        OrderExample example = new OrderExample();
        example.createCriteria().andOrderSnEqualTo(orderSn);
        List<Order> orders = this.orderMapper.selectByExample(example);
        if (orders != null && orders.size() > 0) {
            return orders.get(0);
        }
        return null;
    }

    /**
     * 更新订单的状态 由支付宝自动完成
     *
     * @param params
     * @return
     */
    @Override
    public int updateOrderStatus(Map<String, String> params) {
// 1 验证参数的正确性（代表是支付宝访问，并不是别人伪装的）
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, AliPayConfig.alipay_public_key, AliPayConfig.charset, AliPayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        } //调用SDK验证签名
        if (signVerified) throw new RuntimeException("支付宝验签错误，可能不是支付宝发起的请求");
        //2 修改订单的状态
        String orderSn = params.get("out_trade_no");
        String totalMoeny = params.get("total_amount");
        OrderExample example = new OrderExample();
        example.createCriteria().andOrderSnEqualTo(orderSn);
        List<Order> orders = orderMapper.selectByExample(example);
        if (orders == null || orders.size() == 0) {
            throw new RuntimeException("订单不存在");
        }
        Order order = orders.get(0);
        BigDecimal realTotalMoney = order.getTotalAmount();
        BigDecimal aliPayTotalMoney = new BigDecimal(totalMoeny);
        int result = 0;
        // 金额ok
        if (aliPayTotalMoney.equals(realTotalMoney)) {
            // 处理订单的状态
            order.setPayStatus((byte) PayStatus.SUCCESS_PAY.getCode());
            order.setOrderStatus((byte) OrderStatus.ORDER_COMPLATE.getCode());
            result = orderMapper.updateByPrimaryKey(order);
            // 处理订单记录的状态
            OrderActionExample orderActionExam = new OrderActionExample();
            orderActionExam.createCriteria().andOrderIdEqualTo(order.getId());
            List<OrderAction> orderActions = orderActionMapper.selectByExample(orderActionExam);
            if (orderActions != null && orderActions.size() > 0) {
                OrderAction orderAction = orderActions.get(0);
                orderAction.setActionUser(0);
                orderAction.setActionNote("支付通知付款成功了");
                orderAction.setOrderStatus((byte) OrderStatus.ORDER_COMPLATE.getCode());
                orderAction.setPayStatus((byte) PayStatus.SUCCESS_PAY.getCode());
                result += orderActionMapper.updateByPrimaryKey(orderAction);
            }
        }
        return result;
    }
}
