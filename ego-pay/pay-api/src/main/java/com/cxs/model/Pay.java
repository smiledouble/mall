package com.cxs.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/7 19:40
 * 支付的模型
 */
public class Pay implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final int SACN_CODE_PAY = 1;
    public static final int COMPUTER_PAY = 2;
    /**
     * 支付类型
     */
    @JSONField(serialize = false)
    private int payType;
    /**
     * 订单号
     */
    @JSONField(name = "out_trade_no")
    private String outTradeNo;
    /**
     * 订单名称 subject
     */
    private String subject;
    /**
     * 总金额
     */
    @JSONField(name = "total_amount")
    private String totalAmount;
    /**
     * 商品描述 body
     */
    private String body;
    /**
     * 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
     */
    @JSONField(serialize = false)
    private String storeId;

    /**
     * 超时时间 timeout_express
     */
    @JSONField(name = "timeout_express")
    private String timeoutExpress;

    /**
     * 商品码 product_code
     */
    @JSONField(name = "product_code")
    private String productCode;

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getTimeoutExpress() {
        return timeoutExpress;
    }

    public void setTimeoutExpress(String timeoutExpress) {
        this.timeoutExpress = timeoutExpress;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }
}
