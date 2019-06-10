package com.cxs.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.cxs.domain.Content;
import com.cxs.model.*;
import com.cxs.service.ContentCatService;
import com.cxs.service.ContentService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/24 10:47
 */
@RestController
public class ContentController {

    @Reference
    private ContentService contentService;

    @Reference
    private ContentCatService contentCatService;

    /**
     * 加载内容表格
     *
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("/content/getData")
    public EasyDataGrid contentGetData(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows, @RequestParam(required = true) Integer categoryId) {
        PageBean<Content> pageBean = this.contentService.queryByPage(page, rows, categoryId);
        EasyDataGrid dataGrid = null;
        if (pageBean != null) {
            dataGrid = new EasyDataGrid(pageBean.getTotal(), pageBean.getData());
        }
        return dataGrid;
    }


    /**
     * 懒加载
     *
     * @param id
     * @return
     */
    @GetMapping("/content/cat/getTree")
    public List<EasyUiTree> contentCatgetTree(@RequestParam(defaultValue = "0") Integer id) {
        List<TreeNode> treeNodes = this.contentCatService.loadTreeByParent(id);
        return EasyUiTree.tree2EastUiTree(treeNodes);
    }

    /**
     * 添加或者修改节点
     *
     * @param parentId
     * @param id
     * @param name
     * @return
     */
    @RequestMapping("content/cat/saveOrUpdate")
    public EgoResult saveOrUpdatre(Integer parentId, Integer id, String name) {
        int result = 0;
        TreeNode treeNode = new TreeNode();
        treeNode.setName(name);
        if (parentId != null) {
            //说明是添加操作
            treeNode.setPid(parentId);
            result = contentCatService.addTreeNode(treeNode);
        } else {
            //是修改操作
            treeNode.setId(id);
            result = contentCatService.updateNode(treeNode);
        }
        if (result > 0) {
            return EgoResult.ok();
        }
        return EgoResult.fail(400, "操作失败");
    }

    /**
     * 删除节点的方法
     *
     * @param id
     */
    @RequestMapping("/content/cat/delete/")
    public EgoResult deleteNode(@RequestParam(required = true) Integer id) {
        int i = contentCatService.deleteNode(id);
        if (i > 0) {
            return EgoResult.ok();
        }
        return EgoResult.fail(400, "删除失败");
    }

    /**
     * 添加或者修改内容
     *
     * @return
     */
    @PostMapping("/content/saveOrUpdate")
    public EgoResult contentSaveOrUpdate(Content content) {
        if (content.getId() == null) {
            //说明是添加，调用添加的方法
            content.setCreated(new Date());
            content.setUpdated(new Date());
            Content add = this.contentService.add(content);
            if (add != null) {
                //说明添加成功
                return EgoResult.ok();
            }
        }
        if (content.getId() != null) {
            //说明是修改
            content.setUpdated(new Date());
            int i = this.contentService.update(content);
            if (i > 0) {
                return EgoResult.ok();
            }
        }
        return EgoResult.fail(400, "操作失败");
    }

    /**
     * 删除的方法
     *
     * @param ids
     * @return
     */
    @PostMapping("/content/delete")
    public EgoResult contentDelete(String ids) {
        if (ids == null) {
            throw new RuntimeException("删除时Id不能为空");
        }
        List<Long> list = new ArrayList<>();
        String[] strings = ids.split(",");
        for (String s : strings) {
            list.add(Long.parseLong(s));
        }
        int i = this.contentService.deleteByIds(list);
        if (i > 0) {
            return EgoResult.ok();
        } else {
            return EgoResult.fail(400, "删除失败");
        }
    }

}
