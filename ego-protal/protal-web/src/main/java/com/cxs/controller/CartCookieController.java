package com.cxs.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.cxs.domain.Goods;
import com.cxs.model.EgoResult;
import com.cxs.model.SearchGoodsResult;
import com.cxs.model.SearchGoodsVo;
import com.cxs.service.GoodsService;
import com.cxs.utils.CookieUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/1 20:29
 * 利用cookie完成购物车
 */
//@Controller
public class CartCookieController {


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
    public EgoResult addCart(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) Integer goodsId, @RequestParam(required = true) Integer goodsNum) {
        //从session中得到商品列表 强转成list  获得购物车 转成map集合
        List<Integer> goodsLastList = null;
        String goods_listJson = CookieUtil.getCookieValue(request, "GOODSLIST", true);
        if (goods_listJson != null) {
            goodsLastList = JSON.parseArray(goods_listJson, Integer.class);
        } else {
            goodsLastList = new ArrayList<>();
        }
        if (!goodsLastList.contains(goodsId)) {
            //如果不包含就添加进来
            goodsLastList.add(goodsId);
        }
        Long cartTotal = getCartTotal(request);
        if (cartTotal > 100) {
            return EgoResult.fail(400, "亲，您的购物车已经到达上限呢");
        }


        Map<Integer, Integer> goodsLastInfo = null;
        String goodsInfoJson = CookieUtil.getCookieValue(request, "GOODSINFO", true);
        if (goodsInfoJson != null) {
            goodsLastInfo = JSON.parseObject(goodsInfoJson, Map.class);
        } else {
            goodsLastInfo = new HashMap<>(16);
        }
        Integer total = 0;
        if (goodsLastInfo.containsKey(goodsId)) {
            total = goodsLastInfo.get(goodsId);
        }
        goodsLastInfo.put(goodsId, total + goodsNum);
        //放在cookie中
        CookieUtil.setCookie(request, response, "GOODSLIST", JSON.toJSONString(goodsLastList), 30 * 24 * 3600, true);
        CookieUtil.setCookie(request, response, "GOODSINFO", JSON.toJSONString(goodsLastInfo), 30 * 24 * 3600, true);
        return EgoResult.ok();
    }

    /**
     * 查询总数量
     *
     * @param request
     * @return
     */
    @PostMapping("/cart/getTotal")
    @ResponseBody
    public Long getCartTotal(HttpServletRequest request) {
        String goodsInfoJson = CookieUtil.getCookieValue(request, "GOODSINFO", true);
        long total = 0L;
        if (goodsInfoJson != null) {
            Map<Integer, Integer> goodsInfo = JSON.parseObject(goodsInfoJson, Map.class);
            Set<Integer> goodsIds = goodsInfo.keySet();
            for (Integer goodsId : goodsIds) {
                total += goodsInfo.get(goodsId);
            }
        }
        return total;
    }

    /**
     * 展示列表
     */
    @PostMapping("cart/list/findByPage")
    @ResponseBody
    public SearchGoodsResult showCart(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "1") Integer currentPage, @RequestParam(defaultValue = "10") Integer pageSize) {

        String goods_listJson = CookieUtil.getCookieValue(request, "GOODSLIST", true);
        String goods_infoJson = CookieUtil.getCookieValue(request, "GOODSINFO", true);
        if (goods_listJson == null || goods_infoJson == null) {
            return null;
        }
        List<Integer> goods_list = JSON.parseArray(goods_listJson, Integer.class);

        //   List<Integer> listPage = goods_list.subList((currentPage - 1) * pageSize, currentPage * pageSize > goods_list.size() ? goods_list.size() : currentPage * pageSize);

        Map<Integer, Integer> goods_info = JSON.parseObject(goods_infoJson, Map.class);
        List<Goods> goodss = this.goodsService.findGoodsByIds(goods_list);
        List<SearchGoodsVo> searchGoodsVos = new ArrayList<>();
        goodss.forEach((goods) -> {
            SearchGoodsVo searchGoodsVo = goods2SearchGoodsVo(goods);
            searchGoodsVo.setSize(goods_info.get(goods.getId()));
            searchGoodsVos.add(searchGoodsVo);
        });
        SearchGoodsResult searchGoodsResult = new SearchGoodsResult();
        searchGoodsResult.setPage(currentPage);
        searchGoodsResult.setTotal(goods_list.size());
        searchGoodsResult.setResults(searchGoodsVos);
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
