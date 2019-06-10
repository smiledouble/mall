package com.cxs.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.cxs.domain.Goods;
import com.cxs.domain.GoodsAttribute;
import com.cxs.model.*;
import com.cxs.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/23 14:07
 */
@RestController
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @Reference
    private GoodsCatService goodsCatService;

    @Reference
    private GoodsAttributeService goodsAttributeService;

    @Reference
    private GoodsAttrService goodsAttrService;
    @Reference
    private SolrImportService solrImportService;

    /**
     * 商品分页
     *
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("/goods/getData")
    public EasyDataGrid getData(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows) {
        PageBean<Goods> goodsPageBean = this.goodsService.findAll(page, rows);
        EasyDataGrid data = null;
        if (null != goodsPageBean) {
            data = new EasyDataGrid(goodsPageBean.getTotal(), goodsPageBean.getData());
        }
        return data;
    }

    /**
     * 懒加载树结构
     *
     * @param id
     * @return
     */
    @RequestMapping("/goods/cat/list")
    public List<EasyUiTree> loadTree(@RequestParam(defaultValue = "0") Integer id) {
        List<TreeNode> treeNodes = this.goodsCatService.loadTreeByParent(id);
        return EasyUiTree.tree2EastUiTree(treeNodes);
    }

    /**
     * 新增商品分类
     *
     * @return
     */
    @RequestMapping("/goods/cat/add")
    public ModelAndView addNode() {
        ModelAndView view = new ModelAndView("");
        return view;
    }

    /**
     * 修改商品分类
     *
     * @return
     */
    @RequestMapping("/goods/cat/saveOrUpdate")
    public EgoResult saveOrUpdate(TreeNode node) {
        int i = this.goodsCatService.updateNode(node);
        if (i > 0) {
            return EgoResult.ok();
        } else {
            return EgoResult.fail(400, "修改失败");
        }
    }


    /**
     * 删除商品分类
     *
     * @param id
     * @return
     */
    @RequestMapping("/goods/cat/delete/")
    public EgoResult deleteNode(@RequestParam(required = true) Integer id) {
        int i = this.goodsCatService.deleteNode(id);
        if (i > 0) {
            return EgoResult.ok();
        }
        if (i == -1) {
            return EgoResult.fail(-1, "该节点有子节点，请先删除子节点");
        }
        return EgoResult.fail(-1, "删除失败");
    }

    /**
     * 商品规格的列表
     *
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("/goods/param/list")
    public EasyDataGrid goodParamList(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows) {
        PageBean<GoodsAttribute> pageBean = this.goodsAttributeService.findAll(page, rows);
        EasyDataGrid dataGrid = null;
        if (pageBean != null) {
            dataGrid = new EasyDataGrid(pageBean.getTotal(), pageBean.getData());
        }
        return dataGrid;
    }

    /**
     * 保存商品规格参数
     *
     * @param typeId
     * @param goodsAttribute
     * @return
     */
    @PostMapping("/goods/param/save/{typeId}")
    public EgoResult saveGoodsAttr(@PathVariable("typeId") Short typeId, GoodsAttribute goodsAttribute) {
        goodsAttribute.setTypeId(typeId);
        GoodsAttribute add = this.goodsAttributeService.add(goodsAttribute);
        if (add != null) {
            return EgoResult.ok();
        }
        return EgoResult.fail(400, "添加失败");
    }

    /**
     * 根据商品类型查询商品
     *
     * @param typeId
     * @return
     */
    @RequestMapping("/goods/param/query/itemcatid/{typeId}")
    public Object queryGoodsAttr(@PathVariable("typeId") Short typeId) {
        EGOItemAttrResult result = this.goodsAttributeService.findByTypeId(typeId);
        return result;
    }

    /**
     * 保存商品
     *
     * @param itemParams
     * @param goods
     * @return
     */
    @PostMapping("/goods/save")
    public EgoResult goodsSave(String itemParams, Goods goods) {
        GoodsVo goodsVo = new GoodsVo();
        goodsVo.setGoods(goods);
        goodsVo.setItemParams(itemParams);
        Goods result = this.goodsService.addGoods(goodsVo);
        if (result != null) {
            return EgoResult.ok();
        }

        return EgoResult.fail(400, "添加失败");
    }

    /**
     * 商品内容的回显
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/rest/goods/query/goods/desc/{id}")
    public EgoResult getGoodsContent(@PathVariable("id") Integer goodsId) {
        String goodsDesc = this.goodsService.loadGoodsContent(goodsId);
        if (goodsDesc != null) {
            return EgoResult.ok(goodsDesc);
        }
        return EgoResult.fail(400, "商品的详情加载失败");
    }

    /**
     * 商品规格参数的回显
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/rest/goods/param/goods/query/{id}")
    public EGOItemAttrResult getGoodsAttr(@PathVariable("id") Integer goodsId) {
        List<ItemGroup> itemGroups = this.goodsAttrService.loadAttrValue(goodsId);
        if (itemGroups != null) {
            EGOItemAttrResult egoItemAttrResult = new EGOItemAttrResult();
            egoItemAttrResult.setStatus(200);
            Map<String, List<ItemGroup>> map = new HashMap<>(16);
            map.put("paramData", itemGroups);
            egoItemAttrResult.setData(map);
            return egoItemAttrResult;
        }
        return null;
    }

    /**
     * 修改商品 保存
     *
     * @param itemParams
     * @param goods
     * @return
     */
    @RequestMapping("/goods/update")
    public EgoResult uploadGoods(String itemParams, Goods goods) {
        GoodsVo goodsVo = new GoodsVo();
        goodsVo.setItemParams(itemParams);
        goodsVo.setGoods(goods);
        int result = goodsService.updateGoods(goodsVo);
        if (result > 0) {
            return EgoResult.ok();
        }
        return EgoResult.fail(400, "修改失败");

    }

    /**
     * 商品下架
     *
     * @param ids
     * @return
     */
    @PostMapping("/rest/goods/instock")
    public EgoResult instock(@RequestParam("ids") String ids) {
        String[] split = ids.split(",");
        List<Integer> list = new ArrayList<>();
        for (String s : split) {
            list.add(Integer.parseInt(s));
        }

        Integer i = this.goodsService.instockMulti(list);
        if (i > 0) {
            return EgoResult.ok();
        }
        return EgoResult.fail(400, "操作失败");
    }

    /**
     * 商品上架
     *
     * @return
     */
    @PostMapping("/rest/goods/reshelf")
    public EgoResult reShelf(@RequestParam("ids") String ids) {
        String[] split = ids.split(",");
        List<Integer> list = new ArrayList<>();
        for (String s : split) {
            list.add(Integer.parseInt(s));
        }

        Integer i = this.goodsService.reShelf(list);
        if (i > 0) {
            return EgoResult.ok();
        }
        return EgoResult.fail(400, "操作失败");
    }

    /**
     * 删除商品
     *
     * @param ids
     * @return
     */
    @PostMapping("/rest/goods/delete")
    public EgoResult deleteGoods(@RequestParam("ids") String ids) {
        String[] split = ids.split(",");
        List<Integer> list = new ArrayList<>();
        for (String s : split) {
            list.add(Integer.parseInt(s));
        }
        Integer i = this.goodsService.deleteMulti(list);
        if (i > 0) {
            return EgoResult.ok();
        }
        return EgoResult.fail(400, "操作失败");
    }

    /**
     * 导入商品
     *
     * @return
     */
    @GetMapping("/solr/importDataFromMysql")
    public EgoResult solrImportGoods() {
        long l = this.solrImportService.importAll();
        return EgoResult.ok(l);
    }

}
