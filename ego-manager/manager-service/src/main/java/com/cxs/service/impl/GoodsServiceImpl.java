package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.cxs.domain.Goods;
import com.cxs.domain.GoodsExample;
import com.cxs.mapper.GoodsMapper;
import com.cxs.model.GoodsVo;
import com.cxs.model.ItemGroup;
import com.cxs.model.PageBean;
import com.cxs.service.GoodsAttrService;
import com.cxs.service.GoodsCatService;
import com.cxs.service.GoodsService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/22 21:02
 */
@Service
public class GoodsServiceImpl extends AService<Goods> implements GoodsService {

    @Autowired
    private GoodsMapper dao;

    @Autowired
    private GoodsCatService goodsCatService;

    @Autowired
    private GoodsAttrService goodsAttrService;


    @PostConstruct
    public void setDao() {
        super.dao = this.dao;
    }

    /**
     * 添加商品对象
     * 自动合并事物
     *
     * @param goodsVo
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Goods addGoods(GoodsVo goodsVo) {
        Goods goods = goodsVo.getGoods();
        String itemParams = goodsVo.getItemParams();
        Goods add = super.add(goods);
        //转成对应的规格参数
        List<ItemGroup> itemGroups = JSON.parseArray(itemParams, ItemGroup.class);

        this.goodsAttrService.addMuliGoodsAttr(itemGroups, goods.getId());
        return add;
    }

    /**
     * 根据id查询商品的描述
     *
     * @param goodsId
     */
    @Override
    public String loadGoodsContent(Integer goodsId) {
        Goods goods = super.find(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        return goods.getGoodsContent();
    }

    /**
     * 更新商品
     *
     * @param goodsVo
     * @return
     */
    @Override
    public int updateGoods(GoodsVo goodsVo) {
        Goods goods = goodsVo.getGoods();
        String itemParams = goodsVo.getItemParams();
        int i = super.update(goods);
        List<ItemGroup> itemGroups = JSON.parseArray(itemParams, ItemGroup.class);
        this.goodsAttrService.updateGoodsAttr(itemGroups, goods.getId());
        return i;
    }

    /**
     * 商品下架
     *
     * @param list
     * @return
     */
    @Override
    public Integer instockMulti(List<Integer> list) {
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdIn(list);
        Goods goods = new Goods();

        goods.setIsOnSale((byte) 0);
        int i = this.dao.updateByExampleSelective(goods, example);
        return i;
    }

    /**
     * 商品上架
     *
     * @param list
     * @return
     */
    @Override
    public Integer reShelf(List<Integer> list) {
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdIn(list);
        Goods goods = new Goods();

        goods.setIsOnSale((byte) 1);
        int i = this.dao.updateByExampleSelective(goods, example);
        return i;
    }

    /**
     * 删除商品
     *
     * @param list
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Integer deleteMulti(List<Integer> list) {
        //首先删除商品 再删除他的属性
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdIn(list);
        int i = this.dao.deleteByExample(example);
        //在删除属性
        Integer a = this.goodsAttrService.deleteAttrMulti(list);

        return i;
    }

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
    @Override
    public PageBean<Goods> selectGoods(Integer catId, Integer page, Integer rows, boolean price, boolean comment, boolean common, Integer min, Integer max) {
        List<Integer> cids = this.goodsCatService.selectCatId(catId);
        GoodsExample example = new GoodsExample();
        //分页
        Page<Goods> pagebean = PageHelper.startPage(page, rows);
        if (price) {
            example.setOrderByClause(" shop_price asc ");
        }
        if (comment) {
            example.setOrderByClause(" comment_count desc ");
        }
        GoodsExample.Criteria criteria = example.createCriteria();
        if (min != null && max != null) {
            criteria.andShopPriceBetween(new BigDecimal(min), new BigDecimal(max));
        }
        if (cids != null) {
            criteria.andCatIdIn(cids);
        }
        List<Goods> goods = this.dao.selectByExample(example);
        return new PageBean<Goods>(pagebean.getTotal(), goods, page, rows);
    }

    /**
     * 根据id查询购物车中的商品列表
     *
     * @param goods_list
     * @return
     */
    @Override
    public List<Goods> findGoodsByIds(List<Integer> goods_list) {
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdIn(goods_list);
        List<Goods> goods = this.dao.selectByExample(example);
        return goods;
    }

    /**
     * 商品的库存--
     *
     * @param goodsInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementStock(Map<Integer, Integer> goodsInfo) {
        //根据商品id去 把库存数量减少
        Set<Integer> goodsIds = goodsInfo.keySet();
        for (Integer goodsId : goodsIds) {
            Goods goods = dao.selectByPrimaryKey(goodsId);
            int num = goods.getStoreCount() - goodsInfo.get(goodsId);
            if (num >= 0) {
                //说明可以减少
                goods.setStoreCount((short) num);
                dao.updateByPrimaryKey(goods);
            }
            throw new RuntimeException("库存不能为负值");
        }

    }

    /**
     * 商品的库存回滚补偿
     *
     * @param goodsInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementStock(Map<Integer, Integer> goodsInfo) {
        //根据商品id去 把库存数量回滚
        Set<Integer> goodsIds = goodsInfo.keySet();
        for (Integer goodsId : goodsIds) {
            Goods goods = dao.selectByPrimaryKey(goodsId);
            int num = goods.getStoreCount() + goodsInfo.get(goodsId);
            goods.setStoreCount((short) num);
            dao.updateByPrimaryKey(goods);
        }
    }


}
