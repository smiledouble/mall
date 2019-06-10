package com.cxs.enums;


/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/9 20:34
 */
public enum PayStatus {

    NOT_PAY(0, "未支付"),
    ERROR_PAY(1, "支付失败"),
    SUCCESS_PAY(2, "支付成功");

    private int code;
    private String msg;


    PayStatus(int code, String msg) {
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
