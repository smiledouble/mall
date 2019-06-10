package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.cxs.anno.CleanCache;
import com.cxs.anno.EnableCache;
import com.cxs.domain.ContentCategory;
import com.cxs.domain.ContentCategoryExample;
import com.cxs.mapper.ContentCategoryMapper;
import com.cxs.model.TreeNode;
import com.cxs.service.ContentCatService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/24 11:02
 */
@Service(timeout = 6000, retries = 0)
public class ContentCatServiceImpl implements ContentCatService {

    @Autowired
    private ContentCategoryMapper contentCategoryMapper;


    /**
     * 懒加载树
     *
     * @param id
     * @return
     */
    @Override
    @EnableCache(namespace = "ContentCatServiceImpl", label = "#0")
    public List<TreeNode> loadTreeByParent(Integer id) {
        if (id == null) {
            throw new RuntimeException("父ID不能为空");
        }
        //根据 谁的父id等于这个id 查询出集合
        ContentCategoryExample example = new ContentCategoryExample();
        example.createCriteria().andParentIdEqualTo(id.longValue());
        List<ContentCategory> contentCategories = this.contentCategoryMapper.selectByExample(example);
        List<TreeNode> treeNodes = new ArrayList<>();
        contentCategories.forEach((contentCategory) -> {
            TreeNode treeNode = contentCat2TreeNode(contentCategory);
            treeNodes.add(treeNode);
        });
        return treeNodes;
    }

    /**
     * 将内容类别 转换成treenode
     *
     * @param contentCategory
     * @return
     */
    private TreeNode contentCat2TreeNode(ContentCategory contentCategory) {
        TreeNode treeNode = new TreeNode();
        treeNode.setId(contentCategory.getId().intValue());
        treeNode.setName(contentCategory.getName());
        treeNode.setPid(contentCategory.getParentId().intValue());
        treeNode.setIsOpen(contentCategory.getIsParent() == (byte) 0);
        return treeNode;
    }


    /**
     * 一次性加载内容分类的树
     *
     * @return
     */
    @Override
    @EnableCache(namespace = "ContentCatServiceImpl", label = "EGO_CAT_20190327")
    public List<TreeNode> loadAllTree() {
        return loadAllTree(0L);
    }

    /**
     * 给树增加一个节点
     *
     * @param node 树节点的数据
     * @return 影响的行数
     */
    @Override
    @CleanCache(namespace = "ContentCatServiceImpl", method = "loadTreeByParent", label = "#0.pid")
    public int addTreeNode(TreeNode node) {
        //首先添加节点 再去修改父节点
        ContentCategory parentNode = contentCategoryMapper.selectByPrimaryKey(node.getPid().longValue());
        if (null == parentNode) {
            throw new RuntimeException("父节点不存在");
        }
        ContentCategory contentCategory = new ContentCategory();
        contentCategory.setName(node.getName());
        contentCategory.setParentId(node.getPid().longValue());
        contentCategory.setStatus(1);
        contentCategory.setIsParent((byte) 0);
        contentCategory.setCreated(new Date());
        contentCategory.setUpdated(new Date());
        int i = contentCategoryMapper.insertSelective(contentCategory);
        int result = 0;
        //新增的子节点对父节点的影响
        if (i > 0) {
            if (parentNode.getIsParent() == (byte) 0) {
                //说明之前的父节点下面没有子节点 这里需要变成父节点了
                parentNode.setUpdated(new Date());
                parentNode.setIsParent((byte) 1);
                result = contentCategoryMapper.updateByPrimaryKey(parentNode);
            }
        }
        return i;
    }


    /**
     * 修改该树的节点
     *
     * @param node
     * @return
     */
    @Override
    @CleanCache(namespace = "ContentCatServiceImpl", method = "loadTreeByParent", label = "#0.pid")
    public int updateNode(TreeNode node) {
        Integer id = node.getId();
        ContentCategory contentCategory = contentCategoryMapper.selectByPrimaryKey(id.longValue());
        if (null == contentCategory) {
            throw new RuntimeException("数据不存在");
        }
        contentCategory.setName(node.getName());
        contentCategory.setUpdated(new Date());
        int i = contentCategoryMapper.updateByPrimaryKeySelective(contentCategory);
        return i;
    }

    /**
     * 递归查询全部的树
     *
     * @param i
     * @return
     */
    private List<TreeNode> loadAllTree(Long i) {
        //查询说的父id是这个id
        ContentCategoryExample example = new ContentCategoryExample();
        example.createCriteria().andParentIdEqualTo(i);
        List<ContentCategory> contentCategories = this.contentCategoryMapper.selectByExample(example);
        List<TreeNode> treeNodes = new ArrayList<>();
        contentCategories.forEach((contentCategory) -> {
            TreeNode treeNode = contentCat2TreeNode(contentCategory);
            treeNode.setChildren(loadAllTree(contentCategory.getId()));
            treeNodes.add(treeNode);
        });
        return treeNodes;
    }

    /**
     * 删除该树节点
     * 该节点为子节点，可能影响父节点
     * 该节点为父节点，可能影响子节点
     *
     * @param id
     * @return
     */
    @Override
    @CleanCache(namespace = "ContentCatServiceImpl", method = "loadTreeByParent", label = "#0")
    public int deleteNode(Integer id) {
        //根据id查询自己
        ContentCategory myself = contentCategoryMapper.selectByPrimaryKey(id.longValue());
        if (null == myself) {
            throw new RuntimeException("删除时数据不存在");
        }
        //删除自身
        int i = contentCategoryMapper.deleteByPrimaryKey(myself.getId());
        if (i > 0) {
            //删除成功 要对父节点以及子节点影响
            //对父节点的操作
            handlerParent(myself);
            //在判断对子节点的影响
            handlerChildren(myself);
        }
        return i;
    }

    /**
     * 删除节点时对子节点的影响
     *
     * @param myself
     */
    private void handlerChildren(ContentCategory myself) {
        //通过 删除语句的 where id in（？，？）的做法
        List<Long> childrenIds = getChildrenNode(myself.getId());
        if (childrenIds != null && childrenIds.size() > 0) {
            //执行sql 删除
            ContentCategoryExample example = new ContentCategoryExample();
            example.createCriteria().andIdIn(childrenIds);
            contentCategoryMapper.deleteByExample(example);
        }
    }

    /**
     * 获取子节点
     *
     * @param myself
     * @return
     */
    private List<Long> getChildrenNode(Long id) {
        List<Long> ids = new ArrayList<>();
        ContentCategoryExample example = new ContentCategoryExample();
        example.createCriteria().andParentIdEqualTo(id);
        List<ContentCategory> contentCategories = contentCategoryMapper.selectByExample(example);
        contentCategories.forEach((contentCategory) -> {
            ids.add(contentCategory.getId());
            ids.addAll(getChildrenNode(contentCategory.getId()));
        });
        return ids;
    }

    /**
     * 删除节点时对父节点的影响
     *
     * @param myself
     */
    private void handlerParent(ContentCategory myself) {
        //查询他父节点下面有多少子节点
        ContentCategoryExample example = new ContentCategoryExample();
        example.createCriteria().andParentIdEqualTo(myself.getParentId());
        long count = contentCategoryMapper.countByExample(example);
        if (count == 0) {
            //说明是最后一个子节点呗删除了 应该把自身变成子节点
            ContentCategory contentCategoryTemp = new ContentCategory();
            contentCategoryTemp.setId(myself.getParentId());
            contentCategoryTemp.setUpdated(new Date());
            contentCategoryTemp.setIsParent((byte) 0);
            contentCategoryMapper.updateByPrimaryKeySelective(contentCategoryTemp);
        }
    }

}
