package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.cxs.anno.EnableCache;
import com.cxs.domain.GoodsCategory;
import com.cxs.domain.GoodsCategoryExample;
import com.cxs.mapper.GoodsCategoryMapper;
import com.cxs.model.TreeNode;
import com.cxs.service.GoodsCatService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/23 17:06
 */
@Service(timeout = 7000, retries = 0, actives = 100)
public class GoodsCatServiceImpl implements GoodsCatService {


    @Autowired
    private GoodsCategoryMapper mapper;

    /**
     * 一次性加载树 递归算法
     *
     * @return
     */
    @Override
    @EnableCache(namespace = "GoodsCatServiceImpl", label = "EGO_MENU_20190327")
    public List<TreeNode> loadAllTree() {
        return loadTree((short) 0);
    }

    /**
     * 递归加载所有的树节点
     *
     * @param id
     * @return
     */
    private List<TreeNode> loadTree(short id) {
        //首先查出pid为0的所有一级节点
        GoodsCategoryExample example = new GoodsCategoryExample();
        example.createCriteria().andParentIdEqualTo(id);
        List<GoodsCategory> categories = this.mapper.selectByExample(example);
        //创建目标节点 遍历当前节点 实现递归
        List<TreeNode> treeNodes = new ArrayList<>();
        categories.forEach((category) -> {
            TreeNode treeNode = goodsCat2TreeNode(category);
            //递归开始
            treeNode.setChildren(loadTree(category.getId()));
            treeNodes.add(treeNode);
        });
        return treeNodes;
    }


    /**
     * 懒加载树
     *
     * @param id
     * @return
     */
    @Override
    @EnableCache(namespace = "GoodsCatServiceImpl", label = "#0")
    public List<TreeNode> loadTreeByParent(Integer id) {
        if (id == null) {
            throw new RuntimeException("父id不能为空");
        }
        //创造查询条件
        GoodsCategoryExample example = new GoodsCategoryExample();
        example.createCriteria().andParentIdEqualTo(id.shortValue());
        //查询后返回一层节点集合
        List<GoodsCategory> goodsCategories = this.mapper.selectByExample(example);
        List<TreeNode> treeNodes = new ArrayList<>();
        goodsCategories.forEach((goodsCategory) -> {
            TreeNode treeNode = goodsCat2TreeNode(goodsCategory);
            treeNodes.add(treeNode);
        });
        return treeNodes;
    }

    /**
     * 将商品分类的树 转成TreeNode
     *
     * @param goodsCategory
     * @return
     */
    private TreeNode goodsCat2TreeNode(GoodsCategory goodsCategory) {
        TreeNode treeNode = new TreeNode();
        treeNode.setId(goodsCategory.getId().intValue());
        treeNode.setName(goodsCategory.getName());
        treeNode.setPid(goodsCategory.getParentId().intValue());
        treeNode.setIsOpen(goodsCategory.getLevel() == (byte) 3);
        return treeNode;
    }


    @Override
    public int addTreeNode(TreeNode node) {
        return 0;
    }

    /**
     * 修改树节点
     *
     * @param node
     * @return
     */
    @Override
    public int updateNode(TreeNode node) {
        GoodsCategory goodsCategory = new GoodsCategory();
        goodsCategory.setId(node.getId().shortValue());
        goodsCategory.setName(node.getName());
        int i = mapper.updateByPrimaryKeySelective(goodsCategory);
        return i;
    }

    /**
     * 删除树节点
     *
     * @param id
     * @return
     */
    @Override
    public int deleteNode(Integer id) {
        if (id == null) {
            throw new RuntimeException("删除时Id不能为空");
        }
        GoodsCategory goodsCategory = this.mapper.selectByPrimaryKey(id.shortValue());
        GoodsCategoryExample example = new GoodsCategoryExample();
        example.createCriteria().andParentIdEqualTo(goodsCategory.getId());
        long count = this.mapper.countByExample(example);
        if (count == 0L) {
            return this.mapper.deleteByPrimaryKey(id.shortValue());
        }
        return -1;
    }

    /**
     * 根据父id查询三级id
     *
     * @param catId
     * @return
     */
    @Override
    public List<Integer> selectCatId(Integer catId) {
        List<Integer> cids = new ArrayList<>();
        GoodsCategory mySelf = this.mapper.selectByPrimaryKey(catId.shortValue());
        if (mySelf.getLevel() == (byte) 3) {
            //说明自己就是三级节点
            cids.add(catId);
        } else {
            //创造查询条件
            GoodsCategoryExample example = new GoodsCategoryExample();
            example.createCriteria().andParentIdEqualTo(catId.shortValue());
            List<GoodsCategory> goodsCategories = mapper.selectByExample(example);
            //判断二级 还是一级
            goodsCategories.forEach((goodsCategory) -> {
                if (goodsCategory.getId() == (byte) 3) {
                    cids.add(goodsCategory.getId().intValue());
                } else {
                    cids.addAll(selectCatId(goodsCategory.getId().intValue()));
                }
            });
        }
        return cids;
    }
}
