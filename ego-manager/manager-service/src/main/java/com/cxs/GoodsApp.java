package com.cxs;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/22 21:06
 */
public class GoodsApp {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        ctx.start();

        try {
            System.out.println("manager服务已经上线");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}