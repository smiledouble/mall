package com.cxs.service;

import com.cxs.model.SearchGoodsResult;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/29 16:53
 */
public interface SearchService {
    /**
     * 搜索的方法
     *
     * @param keywords
     * @param page
     * @param rows
     * @param price
     * @param comment
     * @param commmon
     * @param min
     * @param max
     * @return
     */
    SearchGoodsResult doSearch(String keywords, int page, int rows, boolean price, boolean comment, boolean commmon, Integer min, Integer max);

    /**
     * 根据CatId在数据库搜索
     *
     * @param catId
     * @param page
     * @param rows
     * @param price
     * @param comment
     * @param common
     * @param min
     * @param max
     * @return
     */
    SearchGoodsResult doQuery(Integer catId, Integer page, Integer rows, boolean price, boolean comment, boolean common, Integer min, Integer max);
}
