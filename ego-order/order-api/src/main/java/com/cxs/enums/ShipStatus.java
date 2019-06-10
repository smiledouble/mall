package com.cxs.enums;


/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/9 20:34
 */
public enum ShipStatus {

    NOT_SHIP(0, "未发货"),
    SHIPING(1, "运输中"),
    SHIPED(2, "已签收");

    private int code;
    private String msg;


    ShipStatus(int code, String msg) {
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
