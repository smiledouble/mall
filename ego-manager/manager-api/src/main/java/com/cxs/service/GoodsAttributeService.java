package com.cxs.service;

import com.cxs.domain.GoodsAttribute;
import com.cxs.model.EGOItemAttrResult;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/24 10:21
 */
public interface GoodsAttributeService extends IService<GoodsAttribute> {

    /**
     * 根据类型Id查询商品分类信息
     *
     * @param typeId
     * @return
     */
    EGOItemAttrResult findByTypeId(Short typeId);
}
