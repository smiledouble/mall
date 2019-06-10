package com.cxs.enums;


/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/9 20:34
 */
public enum OrderStatus {

    PRE_ORDER(0, "预订单"),
    EXPIRE_ORDER(1, "过期订单"),
    ORDER_COMPLATE(2, "订单完成"),
    CANCLE_ORDER(3, "订单取消");

    private int code;
    private String msg;


    OrderStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }}
