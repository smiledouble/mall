package com.cxs.service;

import com.cxs.model.Pay;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/7 19:46
 * 扫码 和电脑支付两种
 */
public interface AliPayService {

    String pay(Pay pay);

}
