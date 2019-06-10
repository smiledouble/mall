package com.cxs.service;

import com.cxs.domain.Content;
import com.cxs.model.PageBean;

import java.util.List;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/24 10:45
 */
public interface ContentService extends IService<Content> {
    /**
     * 分页查询
     *
     * @param page
     * @param rows
     * @param catId
     * @return
     */
    PageBean queryByPage(Integer page, Integer rows, Integer catId);

    /**
     * 批量删除
     *
     * @param list
     * @return
     */
    int deleteByIds(List<Long> list);

    /**
     * 加载广告
     *
     * @param adCatId
     * @param adNum
     * @return
     */
    List<Content> loadAdContent(Integer adCatId, Integer adNum);
}
