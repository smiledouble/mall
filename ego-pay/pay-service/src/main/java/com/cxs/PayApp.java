package com.cxs;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/7 19:55
 */
public class PayApp {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        ctx.start();
        try {
            System.out.println("支付系统已经启动");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
