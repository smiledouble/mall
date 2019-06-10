package com.cxs;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/3 14:01
 */
public class OrderApp {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        ctx.start();
        try {
            System.out.println("order系统启动了");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
