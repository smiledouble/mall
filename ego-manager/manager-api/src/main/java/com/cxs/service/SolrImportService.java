package com.cxs.service;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/29 22:22
 */
public interface SolrImportService {
    /**
     * 导入商品
     */
    long importAll();

    /**
     * 定时导入 追加
     * @return
     */
    long importAppend();

}
