package com.cxs.service;

import com.cxs.model.SearchGoodsResult;

import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/2 11:40
 */
public interface CartService {
    /**
     * 添加到购物车的接口
     *
     * @param goodsId
     * @param goodsNum
     * @param label
     */
    void addCartUseRedis(Integer goodsId, Integer goodsNum, String label, String token);

    /**
     * 获取总条数
     *
     * @param label
     * @return
     */
    Long getTotal(String label, String token);

    /**
     * 展示购物车的商品
     *
     * @param token
     * @return
     */
    SearchGoodsResult showCart(String label, String token);

    /**
     * 清空预下单的购物车
     *
     * @param goodsInfo
     * @param userId
     */
    void cleanCart(Map<Integer, Integer> goodsInfo, int userId);

    /**
     * 补偿购物车
     *
     * @param goodsInfo
     * @param intValue
     */
    void compensateCart(Map<Integer, Integer> goodsInfo, int intValue);
}
