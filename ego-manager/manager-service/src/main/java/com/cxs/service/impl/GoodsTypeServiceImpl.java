package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.cxs.domain.GoodsType;
import com.cxs.mapper.GoodsTypeMapper;
import com.cxs.service.GoodsTypeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/1 14:31
 */
@Service
public class GoodsTypeServiceImpl implements GoodsTypeService {

    @Autowired
    private GoodsTypeMapper goodsTypeMapper;

    /**
     * 查询所有的商品类型
     *
     * @return
     */
    @Override
    public Map<Integer, String> findAllGoodsType() {
        List<GoodsType> goodsTypes = this.goodsTypeMapper.selectByExample(null);
        Map<Integer, String> map = new HashMap<>(16);
        goodsTypes.forEach((goodsType) -> {
            map.put(goodsType.getId().intValue(), goodsType.getName());
        });
        return map;
    }
}
