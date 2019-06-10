package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.cxs.anno.EnableCache;
import com.cxs.domain.Content;
import com.cxs.domain.ContentExample;
import com.cxs.mapper.ContentMapper;
import com.cxs.model.PageBean;
import com.cxs.service.ContentService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/24 10:45
 */
@Service
public class ContentServiceImpl extends AService<Content> implements ContentService {

    @Autowired
    private ContentMapper dao;

    @PostConstruct
    public void setDao() {
        super.dao = this.dao;
    }

    /**
     * 根据cateId查找
     *
     * @param page
     * @param rows
     * @param catId
     * @return
     */
    @Override
    public PageBean<Content> queryByPage(Integer page, Integer rows, Integer catId) {
        Page<Object> page1 = PageHelper.startPage(page, rows);
        ContentExample example = new ContentExample();
        example.createCriteria().andCategoryIdEqualTo(catId.longValue());
        List<Content> contents = this.dao.selectByExample(example);
        return new PageBean<Content>(page1.getTotal(), contents);
    }

    /**
     * 批量删除
     *
     * @param list
     * @return
     */
    @Override
    public int deleteByIds(List<Long> list) {
        ContentExample example = new ContentExample();
        example.createCriteria().andIdIn(list);
        int i = this.dao.deleteByExample(example);
        return i;
    }

    /**
     * 加载广告
     *
     * @param adCatId
     * @param adNum
     * @return
     */
    @Override
    @EnableCache(namespace = "ContentServiceImpl", label = "#0")
    public List<Content> loadAdContent(Integer adCatId, Integer adNum) {
        ContentExample example = new ContentExample();
        example.setOrderByClause("updated desc");
        example.createCriteria().andCategoryIdEqualTo(adCatId.longValue());
        PageHelper.startPage(1, adNum, false);
        List<Content> contents = this.dao.selectByExample(example);
        return contents;
    }
}
