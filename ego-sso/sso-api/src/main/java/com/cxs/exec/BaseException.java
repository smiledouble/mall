package com.cxs.exec;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/3 10:02
 */
public class BaseException extends RuntimeException {

    private String msg;

    public BaseException(String msg) {
        this.msg = msg;
    }

    public BaseException() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
