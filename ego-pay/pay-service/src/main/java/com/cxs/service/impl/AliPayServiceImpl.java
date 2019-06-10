package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.cxs.config.AliPayConfig;
import com.cxs.model.Pay;
import com.cxs.service.AliPayService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/7 19:47
 */
@Service
public class AliPayServiceImpl implements AliPayService {
    /**
     * 打印日志
     */
    private static Logger log = LoggerFactory.getLogger(AliPayServiceImpl.class);

    @Value("http://www.baidu.com")
    private String nofityUrl;

    private static AlipayTradeService tradeService;

    static {
        //加载配置文件
        Configs.init("zfbinfo.properties");
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Override
    public String pay(Pay pay) {
        if (null == pay) {
            return null;
        }
        log.info("支付开始");
        String result = "";
        switch (pay.getPayType()) {
            case Pay.SACN_CODE_PAY:
                log.info("扫码支付");
                result = getScanPayResult(pay);
                break;
            case Pay.COMPUTER_PAY:
                log.info("电脑支付");
                result = getComputerPayResult(pay);
                break;
            default:
                throw new RuntimeException("不支持的支付方式");
        }
        return result;
    }

    /**
     * 电脑支付
     *
     * @param pay
     * @return
     */
    private String getComputerPayResult(Pay pay) {
        String result = null;
        //初始化alipayclient
        AlipayClient alipayClient = new DefaultAlipayClient(AliPayConfig.gatewayUrl, AliPayConfig.app_id, AliPayConfig.merchant_private_key, "json", AliPayConfig.charset, AliPayConfig.alipay_public_key, AliPayConfig.sign_type);
        //设置请求参数
        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        payRequest.setReturnUrl(AliPayConfig.return_url);
        payRequest.setNotifyUrl(AliPayConfig.notify_url);
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setBody(pay.getBody());
        model.setTotalAmount(pay.getTotalAmount());
        model.setProductCode(pay.getProductCode());
        model.setOutTradeNo(pay.getOutTradeNo());
        model.setSubject(pay.getSubject());
        payRequest.setBizModel(model);
        //发起请求
        try {
            result = alipayClient.pageExecute(payRequest).getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 扫码支付
     *
     * @param pay
     * @return
     */
    private String getScanPayResult(Pay pay) {
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder();
        builder.
                setOutTradeNo(pay.getOutTradeNo())
                .setSubject(pay.getSubject())
                .setBody(pay.getBody())
                .setTotalAmount(pay.getTotalAmount())
                .setTimeoutExpress(pay.getTimeoutExpress())
                .setStoreId(pay.getStoreId())
                .setNotifyUrl(nofityUrl);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功");
                AlipayTradePrecreateResponse response = result.getResponse();
                //打印日志
                dumpResponse(response);
                return response.getQrCode();
            case FAILED:
                log.error("支付宝预下单失败!!");
                return null;
            case UNKNOWN:
                log.error("系统异常，预下单状态未知");
                return null;
            default:
                log.error("不支持的预下单状态，交易异常");
                return null;
        }
    }

    /**
     * 打印日志的
     *
     * @param response
     */
    private void dumpResponse(AlipayTradePrecreateResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }
}