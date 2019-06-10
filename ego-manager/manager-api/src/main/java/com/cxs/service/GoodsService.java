package com.cxs.service;

import com.cxs.domain.Goods;
import com.cxs.model.GoodsVo;
import com.cxs.model.PageBean;

import java.util.List;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/22 21:02
 */
public interface GoodsService extends IService<Goods> {

    /**
     * 添加商品对象
     *
     * @param goodsVo
     * @return
     */
    Goods addGoods(GoodsVo goodsVo);

    /**
     * 根据id查询商品的描述
     *
     * @param goodsId
     * @return
     */
    String loadGoodsContent(Integer goodsId);

    /**
     * 更新商品
     *
     * @param goodsVo
     * @return
     */
    int updateGoods(GoodsVo goodsVo);

    /**
     * 商品下架
     *
     * @param list
     * @return
     */
    Integer instockMulti(List<Integer> list);

    /**
     * 商品上架
     *
     * @param list
     * @return
     */
    Integer reShelf(List<Integer> list);

    /**
     * 删除商品
     *
     * @param list
     * @return
     */
    Integer deleteMulti(List<Integer> list);

    /**
     * 根据添加查询商品
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
    PageBean<Goods> selectGoods(Integer catId, Integer page, Integer rows, boolean price, boolean comment, boolean common, Integer min, Integer max);

    /**
     * 根据id查询购物车中的商品列表
     *
     * @param goods_list
     * @return
     */
    List<Goods> findGoodsByIds(List<Integer> goods_list);

    /**
     * 商品的库存--
     *
     * @param goodsInfo
     */
    void decrementStock(Map<Integer, Integer> goodsInfo);

    /**
     * 商品的库存回滚补偿
     *
     * @param goodsInfo
     */
    void incrementStock(Map<Integer, Integer> goodsInfo);
}
