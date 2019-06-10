package com.cxs.service;

import java.util.List;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/23 17:05
 */
public interface GoodsCatService extends TreeService {
    /**
     * 根据父id查询三级id
     *
     * @param catId
     * @return
     */
    List<Integer> selectCatId(Integer catId);
}
