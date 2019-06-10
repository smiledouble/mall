package com.cxs.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.cxs.model.SearchGoodsResult;
import com.cxs.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/29 16:21
 */
@Controller
public class SearchController {

    @Reference
    private SearchService searchService;

    /**
     * 路由到搜索页面
     *
     * @param keywords
     * @return
     */
    @GetMapping("/search/goSearch")
    public ModelAndView goSearch(String keywords) {
        ModelAndView view = new ModelAndView("search");
        view.addObject("keywords", keywords);
        return view;
    }

    /**
     * 搜搜
     *
     * @param
     * @return
     */
    @PostMapping("/search/doSearch")
    @ResponseBody
    public SearchGoodsResult doSearch(String keywords, String sort, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows, Integer min, Integer max) {
        if (null == sort || "".equals(sort)) {
            return searchService.doSearch(keywords, page, rows, false, true, false, min, max); // 销量asc 价格 desc
        }
        if ("price".equals(sort)) { // 价格
            return searchService.doSearch(keywords, page, rows, true, false, false, min, max);
        }
        if ("comment".equals(sort)) { // 销量
            return searchService.doSearch(keywords, page, rows, false, true, false, min, max);
        }
        if ("common".equals(sort)) { // 评论
            return searchService.doSearch(keywords, page, rows, false, false, true, min, max);
        }
        return null;
    }

    /**
     * 根据catid在数据库搜索的路由
     *
     * @return
     */
    @GetMapping("/goods/cat/{cid}")
    public ModelAndView goSelect(@PathVariable("cid") Integer cid) {
        ModelAndView view = new ModelAndView("search");
        view.addObject("catId", cid);
        return view;
    }

    /**
     * 根据分类id在数据库中搜索
     *
     * @param catId
     * @param sort
     * @param page
     * @param rows
     * @param min
     * @param max
     * @return
     */
    @PostMapping("/search/doQuery")
    @ResponseBody
    public SearchGoodsResult doSelect(Integer catId, String sort, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows, Integer min, Integer max) {
        if (sort == null || sort.equals("")) {
            return searchService.doQuery(catId, page, rows, false, false, false, min, max);
        }
        if ("price".equals(sort)) {
            return searchService.doQuery(catId, page, rows, true, false, false, min, max);
        }
        if ("comment".equals(sort)) {
            return searchService.doQuery(catId, page, rows, false, true, false, min, max);
        }
        if ("common".equals(sort)) {
            return searchService.doQuery(catId, page, rows, false, false, true, min, max);
        }
        return null;

    }
}