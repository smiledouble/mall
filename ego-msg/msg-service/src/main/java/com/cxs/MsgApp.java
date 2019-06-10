package com.cxs;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/7 16:37
 */
public class MsgApp {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        ctx.start();
        try {
            System.out.println("消息系统已经上线");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
