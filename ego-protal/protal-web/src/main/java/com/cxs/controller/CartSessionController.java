package com.cxs.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.cxs.domain.Goods;
import com.cxs.model.EgoResult;
import com.cxs.model.SearchGoodsResult;
import com.cxs.model.SearchGoodsVo;
import com.cxs.service.GoodsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/1 19:34
 * 最原始的将商品存放在session中的做法
 */
//@Controller
public class CartSessionController {

    @Reference
    private GoodsService goodsService;

    /**
     * 路由
     *
     * @return
     */
    @GetMapping("/cart/list")
    public ModelAndView catrList() {
        ModelAndView view = new ModelAndView("cart-list");
        return view;
    }

    /**
     * 添加到购物车
     *
     * @return
     */
    @PostMapping("/cart/add")
    @ResponseBody
    public EgoResult addCart(HttpSession session, @RequestParam(required = true) Integer goodsId, @RequestParam(required = true) Integer goodsNum) {
        //从session中得到商品列表 强转成list  获得购物车 转成map集合
        List<Integer> goods_list = (List<Integer>) session.getAttribute("GOODS_LIST");
        //如果获得的列表为空 说明之前没有加入列表 就创建一个
        if (goods_list == null) {
            goods_list = new ArrayList<>();
        }
        if (!goods_list.contains(goodsId)) {
            //如果不包含这个key 就添加进来
            goods_list.add(goodsId);
        }
        Map<Integer, Integer> goods_info = (HashMap<Integer, Integer>) session.getAttribute("GOODS_INFO");
        if (goods_info == null) {
            //说明之前没有加入购物车 就创建一个
            goods_info = new HashMap<>(16);
        }
        Integer total = 0;
        if (goods_info.containsKey(goodsId)) {
            //如果之前包含了 就先得到
            total = goods_info.get(goodsId);
        }
        goods_info.put(goodsId, total + goodsNum);

        //存在session里面
        session.setAttribute("GOODS_LIST", goods_list);
        session.setAttribute("GOODS_INFO", goods_info);

        return EgoResult.ok();
    }

    /**
     * 获得总数量
     */
    @PostMapping("/cart/getTotal")
    @ResponseBody
    public Long getCartTotal(HttpSession session) {
        Long total = 0L;
        Map<Integer, Integer> goods_info = (HashMap<Integer, Integer>) session.getAttribute("GOODS_INFO");
        if (goods_info != null) {
            Set<Integer> keySet = goods_info.keySet();
            for (Integer k : keySet) {
                total += goods_info.get(k);
            }
        }
        return total;
    }

    /**
     * 分页展示购物车的商品
     */
    @PostMapping("/cart/list/findByPage")
    @ResponseBody
    public SearchGoodsResult showCart(HttpSession session, @RequestParam(defaultValue = "1") Integer currentPage, @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Integer> goods_list = (List<Integer>) session.getAttribute("GOODS_LIST");
        Map<Integer, Integer> goods_info = (HashMap<Integer, Integer>) session.getAttribute("GOODS_INFO");
        if (goods_list == null || goods_info == null) {
            //如果商品列表或购物车为空 直接返回空
            return null;
        }
        //根据商品的id 去查询商品集合
        SearchGoodsResult searchGoodsResult = new SearchGoodsResult();
        List<SearchGoodsVo> list = new ArrayList<>();
        List<Goods> goodss = this.goodsService.findGoodsByIds(goods_list);
        if (goodss != null) {
            goodss.forEach((goods) -> {
                SearchGoodsVo searchGoodsVo = goods2SearchGoodsVo(goods);
                searchGoodsVo.setSize(goods_info.get(goods.getId()));
                list.add(searchGoodsVo);

            });
        }
        searchGoodsResult.setPage(currentPage);
        searchGoodsResult.setPage(pageSize);
        searchGoodsResult.setResults(list);
        searchGoodsResult.setTotal(goods_list.size());
        return searchGoodsResult;
    }

    /**
     * goods转vo对象
     *
     * @param goods
     * @return
     */
    private SearchGoodsVo goods2SearchGoodsVo(Goods goods) {
        SearchGoodsVo searchGoodsVo = new SearchGoodsVo();
        searchGoodsVo.setId(goods.getId().toString());
        searchGoodsVo.setGoodsNameHl(goods.getGoodsName());
        searchGoodsVo.setGoodsPrice(goods.getShopPrice().toString());
        searchGoodsVo.setGoodsImg(goods.getOriginalImg());
        return searchGoodsVo;
    }


}
