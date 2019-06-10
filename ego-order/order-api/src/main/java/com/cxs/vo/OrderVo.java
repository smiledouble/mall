package com.cxs.vo;

import com.cxs.domain.Admin;
import com.cxs.model.SearchGoodsVo;

import java.io.Serializable;
import java.util.List;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/8 20:19
 */
public class OrderVo implements Serializable {
    private List<SearchGoodsVo> goodsVoList;
    private String outTradeNo;
    private String totalAmount;
    private String orderName;
    private String orderInfo;

    private Admin user;

    public List<SearchGoodsVo> getGoodsVoList() {
        return goodsVoList;
    }

    public void setGoodsVoList(List<SearchGoodsVo> goodsVoList) {
        this.goodsVoList = goodsVoList;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Admin getUser() {
        return user;
    }

    public void setUser(Admin user) {
        this.user = user;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
}
