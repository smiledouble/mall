package com.cxs.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.cxs.domain.Admin;
import com.cxs.domain.Order;
import com.cxs.enums.OrderStatus;
import com.cxs.model.EgoResult;
import com.cxs.model.PageBean;
import com.cxs.model.SearchGoodsVo;
import com.cxs.redis.RedisClient;
import com.cxs.service.OrderService;
import com.cxs.utils.CookieUtil;
import com.cxs.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/8 20:22
 */
@Controller
public class OrderController {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private RedisClient redis;

    @Reference
    private OrderService orderService;

    /**
     * 下订单 利用redis实现异步提交订单
     *
     * @param orderVo
     * @return
     */
    @RequestMapping("/order/submit")
    @ResponseBody
    public String downOrder(HttpServletRequest request, String callBackFunc, OrderVo orderVo) {
        //得到所有的商品
        List<SearchGoodsVo> goodsVoList = orderVo.getGoodsVoList();
        //用来放在redis里面 存商品的库存的
        Map<String, String> goodsStomeNum = new HashMap<>(16);
        for (SearchGoodsVo searchGoodsVo : goodsVoList) {
            //首先将商品的名称转码一下
            String goodsName = searchGoodsVo.getGoodsNameHl();
            searchGoodsVo.setGoodsNameHl(goodsName);
            //得到商品的id 和库存数量
            String goodsId = searchGoodsVo.getId();
            //得到商品的数量
            Integer size = searchGoodsVo.getSize();
            //从redis里面获取
            String value = redis.get("store:" + goodsId);
            if (value != null) {
                //说明获取到了，就做库存的判断
                int num = Integer.valueOf(value) - size;
                if (num < 0) {
                    //如果小于0
                    StringBuffer sb = new StringBuffer(callBackFunc + "(");
                    sb.append(JSON.toJSONString(EgoResult.fail(503, "商品" + searchGoodsVo.getGoodsNameHl() + "的库存不足,剩余：" + value)));
                    sb.append(")");
                    return sb.toString();
                }
                //剩余的库存
                goodsStomeNum.put(goodsId, num + "");
            } else {
                StringBuffer sb = new StringBuffer(callBackFunc + "(");
                sb.append(JSON.toJSONString(EgoResult.fail(504, "商品" + searchGoodsVo.getGoodsNameHl() + "不存在")));
                sb.append(")");
                return sb.toString();
            }
        }
        //从cookie里面获取token 再从redis 里面取出来
        String token = CookieUtil.getCookieValue(request, "X-TOKEN");
        String adminJson = redis.get(token);
        Admin admin = JSON.parseObject(adminJson, Admin.class);
        orderVo.setUser(admin);
        //从线程局部变量里面取
//        Admin user = (Admin) ThreadLocalUtil.getVal("USER");
//        orderVo.setUser(user);
        //把订单放在队列里面
        jmsTemplate.convertAndSend("pre.order.queue", orderVo);
        //更新缓存中的数据
        goodsStomeNum.forEach((goodsId, goodsStore) -> {
            redis.set("store:" + goodsId, goodsStore);
        });

        StringBuffer sb = new StringBuffer(callBackFunc + "(");
        sb.append(JSON.toJSONString(EgoResult.ok()));
        sb.append(")");
        return sb.toString();
    }

    /**
     * 跳转到检查订单页面
     *
     * @return
     */
    @GetMapping("/order/check")
    public ModelAndView toCheck() {
        ModelAndView view = new ModelAndView("check");
        return view;
    }

    /**
     * 查询用户的订单 默认预订单
     *
     * @param status
     * @return
     */
    @PostMapping("/order/query")
    @ResponseBody
    public EgoResult queryOrder(HttpServletRequest request, Integer status, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows) {
        //根据登录的用户id和 订单状态 查询订单
        String token = CookieUtil.getCookieValue(request, "X-TOKEN");
        String adminJson = redis.get(token);
        Admin user = JSON.parseObject(adminJson, Admin.class);
        OrderStatus orderStatus = null;
        if (status == 0) {
            orderStatus = OrderStatus.PRE_ORDER;
        } else if (status == 1) {
            orderStatus = OrderStatus.EXPIRE_ORDER;
        } else if (status == 2) {
            orderStatus = OrderStatus.ORDER_COMPLATE;
        } else {
            orderStatus = OrderStatus.CANCLE_ORDER;
        }
        PageBean<Order> orderPageBean = orderService.queryOrderByPage(user.getId(), orderStatus, page, rows);

        return EgoResult.ok(orderPageBean);
    }


}
