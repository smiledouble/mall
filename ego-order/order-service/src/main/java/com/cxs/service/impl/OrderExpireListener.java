package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.cxs.domain.*;
import com.cxs.enums.OrderStatus;
import com.cxs.enums.PayStatus;
import com.cxs.mapper.OrderActionMapper;
import com.cxs.mapper.OrderGoodsMapper;
import com.cxs.mapper.OrderMapper;
import com.cxs.redis.RedisClient;
import com.cxs.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/9 20:52
 * 延时队列的监听
 */
@Component
public class OrderExpireListener implements MessageListener {

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


    @Override
    @JmsListener(containerFactory = "containerFactory", destination = "pre.order", concurrency = "2")
    public void onMessage(Message message) {
        //到了指定的时间 如果没有付款 就将订单状态修改
        TextMessage textMessage = (TextMessage) message;
        String orderId = null;
        try {
            orderId = textMessage.getText();
            message.acknowledge();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        if (orderId != null) {
            //就修改订单状态吧
            handlerPreOrder(orderId);
        }
    }

    /**
     * 过期未支付 修改订单状态
     *
     * @param orderId
     */
    private void handlerPreOrder(String orderId) {
        //根据订单的id查询出这个订单
        Order order = orderMapper.selectByPrimaryKey(Integer.valueOf(orderId));
        if (order == null) {
            throw new RuntimeException("订单不能为空");
        }
        if (order.getPayStatus() == PayStatus.NOT_PAY.getCode()) {
            //如果没有支付 就修改状态
            order.setOrderStatus((byte) OrderStatus.EXPIRE_ORDER.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
            //orderGoods的修改
            OrderGoodsExample example = new OrderGoodsExample();
            example.createCriteria().andOrderIdEqualTo(order.getId());
            List<OrderGoods> orderGoods = orderGoodsMapper.selectByExample(example);
            //用于回滚数据库的库存的集合
            Map<Integer, Integer> map = new HashMap<>(16);
            orderGoods.forEach((orderGood) -> {
                //库存的补偿 redis
                String num = redis.get("store:" + orderGood.getGoodsId());
                redis.set("store:" + orderGood.getGoodsId(), Integer.valueOf(num) + orderGood.getGoodsNum() + "");
                map.put(orderGood.getGoodsId(), Integer.valueOf(orderGood.getGoodsNum()));
            });
            //补偿数据库的库存
            goodsService.incrementStock(map);
            //orderAction的修改
            OrderActionExample oaExample = new OrderActionExample();
            oaExample.createCriteria().andOrderIdEqualTo(order.getId());
            List<OrderAction> orderActions = orderActionMapper.selectByExample(oaExample);
            if (orderActions != null && orderActions.size() > 0) {
                orderActions.forEach((orderAction) -> {
                    //mq自动修改的
                    orderAction.setActionUser(0);
                    orderAction.setActionNote("订单过期");
                    orderAction.setOrderStatus((byte) OrderStatus.EXPIRE_ORDER.getCode());
                    orderActionMapper.updateByPrimaryKeySelective(orderAction);
                });
            }
        }
    }
}
