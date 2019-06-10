package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.cxs.domain.GoodsAttr;
import com.cxs.domain.GoodsAttrExample;
import com.cxs.mapper.GoodsAttrMapper;
import com.cxs.model.ItemGroup;
import com.cxs.service.GoodsAttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/26 20:20
 */
@Service
public class GoodsAttrServiceImpl extends AService<GoodsAttr> implements GoodsAttrService {

    @Autowired
    private GoodsAttrMapper dao;

    @PostConstruct
    public void setDao() {
        super.dao = this.dao;
    }

    /**
     * 批量添加属性
     *
     * @param itemGroups
     * @param id
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void addMuliGoodsAttr(List<ItemGroup> itemGroups, Integer goodsId) {
        itemGroups.forEach((itemGroup) -> {
            GoodsAttr goodsAttr = new GoodsAttr();
            goodsAttr.setGoodsId(goodsId);
            goodsAttr.setAttrId(itemGroup.getCatId().shortValue());
            //拼接
            StringBuffer sb = new StringBuffer("{\"" + itemGroup.getGroup() + "\":");
            sb.append(JSON.toJSONString(itemGroup.getParams()));
            sb.append("}");

            goodsAttr.setAttrValue(sb.toString());
            dao.insert(goodsAttr);
        });

    }

    /**
     * 根据id查询属性值 封装对应的对象
     *
     * @param goodsId
     * @return
     */
    @Override
    public List<ItemGroup> loadAttrValue(Integer goodsId) {
        if (goodsId == null) {
            throw new RuntimeException("查询时id不能为空");
        }
        GoodsAttrExample example = new GoodsAttrExample();
        example.createCriteria().andGoodsIdEqualTo(goodsId);
        List<GoodsAttr> goodsAttrs = this.dao.selectByExampleWithBLOBs(example);
        List<ItemGroup> itemGroups = new ArrayList<>();
        goodsAttrs.forEach((goodsAttr) -> {
            itemGroups.add(goodsAttr2ItemGroup(goodsAttr));
        });
        return itemGroups;
    }

    /**
     * 修改商品参数的方法
     *
     * @param itemGroups
     * @param id
     */
    @Override
    public void updateGoodsAttr(List<ItemGroup> itemGroups, Integer id) {
        for (ItemGroup ig : itemGroups) {
            Integer catId = ig.getCatId();
            String group = ig.getGroup();
            //构造查询条件 两个id定位一个属性值
            GoodsAttrExample example = new GoodsAttrExample();
            example.createCriteria().andGoodsIdEqualTo(id)
                    .andAttrIdEqualTo(catId.shortValue());
            //拼接
            StringBuffer sb = new StringBuffer("{\"" + group + "\":");
            sb.append(JSON.toJSONString(ig.getParams()));
            sb.append("}");
            //查询出来 其实只有一个
            List<GoodsAttr> goodsAttrs = dao.selectByExample(example);
            if (goodsAttrs != null && goodsAttrs.size() > 0) {
                //取到这一个 设置值 保存 注意是大数据类型 text
                GoodsAttr goodsAttr = goodsAttrs.get(0);
                goodsAttr.setAttrValue(sb.toString());
                dao.updateByPrimaryKeyWithBLOBs(goodsAttr);
            }
        }
    }

    /**
     * 根据商品id删除
     *
     * @param list
     * @return
     */
    @Override
    public Integer deleteAttrMulti(List<Integer> list) {
        GoodsAttrExample example = new GoodsAttrExample();
        example.createCriteria().andGoodsIdIn(list);
        int i = this.dao.deleteByExample(example);
        return i;
    }

    /**
     * 将属性对象转成对应的对象
     *
     * @param goodsAttr
     * @return
     */
    private ItemGroup goodsAttr2ItemGroup(GoodsAttr goodsAttr) {
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setCatId(goodsAttr.getAttrId().intValue());
        String json = goodsAttr.getAttrValue();

        Map map = JSON.parseObject(json, Map.class);
        String group = "";
        String jsonParam = null;
        Set set = map.keySet();
        for (Object k : set) {
            group = k.toString();
            jsonParam = map.get(k).toString();
        }
        List<Object> parmas = JSON.parseArray(jsonParam, Object.class);
        itemGroup.setParams(parmas);
        itemGroup.setGroup(group);
        return itemGroup;
    }
}
