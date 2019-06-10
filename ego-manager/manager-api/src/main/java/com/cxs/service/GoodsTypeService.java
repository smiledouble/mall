package com.cxs.service;

import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/1 14:30
 */
public interface GoodsTypeService {
    /**
     * 查询所有的商品类型
     * @return
     */
    Map<Integer,String> findAllGoodsType();

}
