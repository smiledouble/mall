package com.cxs.service;

import com.cxs.domain.Order;
import com.cxs.enums.OrderStatus;
import com.cxs.model.PageBean;
import com.cxs.vo.OrderVo;

import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/8 20:19
 */
public interface OrderService {
    /**
     * 分页查询订单
     *
     * @param id
     * @param orderStatus
     * @param page
     * @param rows
     * @return
     */
    PageBean<Order> queryOrderByPage(Short id, OrderStatus orderStatus, Integer page, Integer rows);

    /**
     * 根据订单号 查询订单
     *
     * @param orderSn
     * @return
     */
    OrderVo loadOrderByOrderSn(String orderSn);

    /**
     * 检查数据库订单是否支付完成
     *
     * @param orderSn
     * @return
     */
    Order query(String orderSn);

    /**
     * 更新订单的状态 由支付宝自动完成
     *
     * @param params
     * @return
     */
    int updateOrderStatus(Map<String, String> params);
}
