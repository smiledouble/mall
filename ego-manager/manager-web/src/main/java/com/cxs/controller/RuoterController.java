package com.cxs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/23 13:58
 */
@Controller
public class RuoterController {

    /**
     * 跳转到主界面
     *
     * @return
     */
    @GetMapping("/toIndex")
    public ModelAndView toIndex() {
        ModelAndView view = new ModelAndView("index");
        return view;
    }

    /**
     * 跳转到商品页面
     *
     * @param page
     * @return
     */
    @GetMapping("/goods/{page}")
    public String routerGoods(@PathVariable("page") String page) {
        return "goods/goods-" + page;
    }

    /**
     * 跳转到分类页面
     *
     * @param page
     * @return
     */
    @GetMapping("/content/{page}")
    public String routerContent(@PathVariable("page") String page) {
        return "content/content-" + page;
    }

}