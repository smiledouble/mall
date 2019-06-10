package com.cxs;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/29 21:56
 */
public class ProtalApp {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        ctx.start();
//        SearchService searchService = ctx.getBean(SearchService.class);
//        boolean flag = true;
//        while (flag) {
//            try {
//                Thread.sleep(300);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            SearchGoodsResult result = searchService.doSearch("小米", 1, 20);
//            String jsonString = JSON.toJSONString(result);
//            System.out.println(jsonString);
//        }
        System.out.println("protal服务开启");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
