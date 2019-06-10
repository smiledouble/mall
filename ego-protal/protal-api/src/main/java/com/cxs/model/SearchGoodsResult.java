package com.cxs.model;

import java.io.Serializable;
import java.util.List;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/29 16:49
 * 查询商品的返回对象
 */
public class SearchGoodsResult implements Serializable {

    private List<SearchGoodsVo> results;

    private Integer page;

    private long total;


    public List<SearchGoodsVo> getResults() {
        return results;
    }

    public void setResults(List<SearchGoodsVo> results) {
        this.results = results;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
