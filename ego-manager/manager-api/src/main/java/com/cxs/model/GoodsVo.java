package com.cxs.model;

import com.cxs.domain.Goods;

import java.io.Serializable;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/26 20:22
 */
public class GoodsVo implements Serializable {

    private Goods goods;

    private String itemParams;

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public String getItemParams() {
        return itemParams;
    }

    public void setItemParams(String itemParams) {
        this.itemParams = itemParams;
    }

    public GoodsVo() {
    }

    public GoodsVo(Goods goods, String itemParams) {
        this.goods = goods;
        this.itemParams = itemParams;
    }
}
