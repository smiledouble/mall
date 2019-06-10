package com.cxs.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.cxs.domain.Content;
import com.cxs.model.EgoResult;
import com.cxs.model.TreeNode;
import com.cxs.service.ContentService;
import com.cxs.service.GoodsCatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/27 10:14
 */
@RestController
public class HomeController {

    @Reference
    private GoodsCatService goodsCatService;

    @Reference
    private ContentService contentService;
    @Value("${ad.adCatId}")
    private Integer adCatId;
    @Value("${ad.num}")
    private Integer adNum;

    /**
     * 加载所有的菜单
     *
     * @return
     */
    @GetMapping("goods/cat/getAllMenu")
    public List<TreeNode> loadMenuTree() {
        List<TreeNode> treeNodes = this.goodsCatService.loadAllTree();
        if (treeNodes != null) {
            return treeNodes;
        }
        return null;
    }

    /**
     * 广告展示
     *
     * @return
     */
    @GetMapping("content/ad/getData")
    public EgoResult loadAdData() {
        List<Content> contents = this.contentService.loadAdContent(adCatId, adNum);
        if (contents != null && contents.size() > 0) {
            return EgoResult.ok(contents);
        }
        return EgoResult.fail(400, "加载广告失败");
    }

}
