package com.cxs.service;

import com.cxs.domain.GoodsAttr;
import com.cxs.model.ItemGroup;

import java.util.List;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/26 20:19
 */
public interface GoodsAttrService extends IService<GoodsAttr> {


    /**
     * 批量添加属性
     *
     * @param itemGroups
     * @param id
     */
    void addMuliGoodsAttr(List<ItemGroup> itemGroups, Integer id);

    /**
     * 根据id查询属性值 封装对应的对象
     *
     * @param goodsId
     * @return
     */
    List<ItemGroup> loadAttrValue(Integer goodsId);

    /**
     * 修改商品参数的方法
     *
     * @param itemGroups
     * @param id
     */
    void updateGoodsAttr(List<ItemGroup> itemGroups, Integer id);

    /**
     * 根据商品id删除
     *
     * @param list
     * @return
     */
    Integer deleteAttrMulti(List<Integer> list);
}
