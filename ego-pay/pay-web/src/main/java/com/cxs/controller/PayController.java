package com.cxs.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.cxs.domain.Order;
import com.cxs.model.EgoResult;
import com.cxs.model.Pay;
import com.cxs.service.AliPayService;
import com.cxs.service.OrderService;
import com.cxs.vo.OrderVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/8 9:38
 */
@Controller
public class PayController {

    @Reference
    private AliPayService aliPayService;

    @Reference
    private OrderService orderService;

    /**
     * 路由
     *
     * @return
     */
    @GetMapping("/toPay")
    public ModelAndView toPay(String orderSn) {
        ModelAndView view = new ModelAndView("pay");
        OrderVo orderVo = orderService.loadOrderByOrderSn(orderSn);
        view.addObject("order", orderVo);
        return view;
    }

    /**
     * 支付的方法
     *
     * @param pay
     * @return
     */
    @PostMapping("/doPay")
    @ResponseBody
    public EgoResult doPay(Pay pay) {
        //productCode = FAST_INSTANT_TRADE_PAY
        //storeId = TEST_STORE_ID
        String result = aliPayService.pay(pay);
        if (result != null) {
            return EgoResult.ok(result);
        }
        return EgoResult.fail(503, "支付失败");
    }

    /**
     * 查询订单的状态情况
     *
     * @param orderSn
     * @return
     */
    @PostMapping("/order/query")
    @ResponseBody
    public EgoResult queryOrder(@RequestParam(required = true) String orderSn) {
        Order order = orderService.query(orderSn);
        return EgoResult.ok(order);
    }


    /**
     * 检查订单是否支付成功
     *
     * @param orderSn
     * @return
     */
    @GetMapping("/pay/ok")
    public ModelAndView ok(String orderSn) {
        ModelAndView view = new ModelAndView("ok");
        view.addObject("orderSn", orderSn);
        return view;
    }

    /**
     * 支付宝会访问我们该链接
     * 需要哪些参数： 订单号 ，总金额
     */
    @RequestMapping("/pay/notify")
    public EgoResult notify(HttpServletRequest request) {
        /**
         * 支付宝请求的参数
         */
        Map<String, String> params = new HashMap<String, String>();

        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            try {
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            params.put(name, valueStr);
        }
        int result = orderService.updateOrderStatus(params);
        if (result > 0) {
            return EgoResult.ok("OK");
        }
        return null;

    }
}