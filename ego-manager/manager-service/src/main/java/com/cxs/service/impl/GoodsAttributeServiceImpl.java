package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.cxs.domain.GoodsAttribute;
import com.cxs.domain.GoodsAttributeExample;
import com.cxs.mapper.GoodsAttributeMapper;
import com.cxs.model.EGOItemAttrResult;
import com.cxs.model.ItemGroup;
import com.cxs.service.GoodsAttributeService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/24 10:21
 */
@Service
public class GoodsAttributeServiceImpl extends AService<GoodsAttribute> implements GoodsAttributeService {

    @Autowired
    private GoodsAttributeMapper dao;

    @PostConstruct
    public void setDao() {
        super.dao = this.dao;
    }


    /**
     * 根据类型Id查询商品分类信息
     *
     * @param typeId
     * @return
     */
    @Override
    public EGOItemAttrResult findByTypeId(Short typeId) {
        if (typeId == null) {
            throw new RuntimeException("查询时类型Id不能为空");
        }
        GoodsAttributeExample example = new GoodsAttributeExample();
        example.createCriteria().andTypeIdEqualTo(typeId);
        //根据类型id查询对象集合
        List<GoodsAttribute> goodsAttributes = this.dao.selectByExampleWithBLOBs(example);
        //创建返回的对象
        EGOItemAttrResult egoItemAttrResult = new EGOItemAttrResult();
        if (goodsAttributes != null && goodsAttributes.size() > 0) {
            //如果查出来的集合不为空，就组装对象
            egoItemAttrResult.setStatus(200);
            Map<String, List<ItemGroup>> datas = new HashMap<>(16);
            List<ItemGroup> list = goodsAttribute2ItemGroup(goodsAttributes);
            datas.put("paramData", list);
            egoItemAttrResult.setData(datas);
        }
        return egoItemAttrResult;
    }

    /**
     * goodAttribute转成ItemGroup对象
     *
     * @param goodsAttributes
     * @return
     */
    private List<ItemGroup> goodsAttribute2ItemGroup(List<GoodsAttribute> goodsAttributes) {
        List<ItemGroup> itemGroups = new ArrayList<>();
        goodsAttributes.forEach((goodsAttribute) -> {
            ItemGroup itemGroup = new ItemGroup();
            itemGroup.setCatId(goodsAttribute.getId());
            itemGroup.setGroup(goodsAttribute.getAttrName());
            String attrValues = goodsAttribute.getAttrValues();
            String space = " ";
            if (attrValues != null && !"".equals(attrValues.replace(space, ""))) {
                itemGroup.setParams(Arrays.asList(attrValues.split("\r\n")));
            } else {
                itemGroup.setParams(Arrays.asList("值"));
            }
            itemGroups.add(itemGroup);
        });
        return itemGroups;
    }
}
