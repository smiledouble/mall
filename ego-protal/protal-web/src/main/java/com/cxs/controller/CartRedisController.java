package com.cxs.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.cxs.model.EgoResult;
import com.cxs.model.SearchGoodsResult;
import com.cxs.service.CartService;
import com.cxs.service.GoodsService;
import com.cxs.utils.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/2 11:39
 */
@Controller
public class CartRedisController {

    @Reference
    private GoodsService goodsService;

    @Reference
    private CartService cartService;

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
    public EgoResult addCart(HttpServletRequest request, @RequestHeader("label") String label, @RequestParam(required = true) Integer goodsId, @RequestParam(required = true) Integer goodsNum) {
        String token = null;
        try {
            token = CookieUtil.getCookieValue(request, "X-TOKEN");
        } catch (Exception e) {

        }
        long total = this.cartService.getTotal(label, token);
        if (total > 100) {
            return EgoResult.fail(400, "商品数量大于100哟，亲");
        }
        this.cartService.addCartUseRedis(goodsId, goodsNum, label, token);
        return EgoResult.ok();
    }

    /**
     * 获取购物车里面商品的总数量
     *
     * @return
     */
    @RequestMapping("/cart/getTotal")
    @ResponseBody
    public Long getTotal(HttpServletRequest request, @RequestHeader("label") String label) {
        String token = null;
        try {
            token = CookieUtil.getCookieValue(request, "X-TOKEN");
        } catch (Exception e) {

        }
        return cartService.getTotal(label, token);
    }

    /**
     * 生成购物车cookie
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/cart/getCookie")
    @ResponseBody
    public EgoResult getCartCookie(HttpServletRequest request, HttpServletResponse response) {
        String label = UUID.randomUUID().toString();
        CookieUtil.setCookie(request, response, "EGO-CART-LABEL", label, true);
        return EgoResult.ok();
    }

    /**
     * 展示购物车商品列表
     *
     * @return
     */
    @PostMapping("/cart/list/findByPage")
    @ResponseBody
    public SearchGoodsResult showCartGoods(HttpServletRequest request) {
        //判断用户是否登录
        String token = CookieUtil.getCookieValue(request, "X-TOKEN");
        String label = CookieUtil.getCookieValue(request, "EGO-CART-LABEL");
        SearchGoodsResult searchGoodsResult = this.cartService.showCart(label, token);
        if (searchGoodsResult != null) {
            return searchGoodsResult;
        }
        return null;
    }


}
