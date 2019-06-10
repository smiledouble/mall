package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.cxs.model.TokenResult;
import com.cxs.model.WeChatMessage;
import com.cxs.service.WeChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/4 13:26
 */
@Service
public class WeChatMessageServiceImpl implements WeChatMessageService, MessageListener {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${appid}")
    private String appId;

    /**
     * 密码
     */
    @Value("${secret}")
    private String secret;

    @Value("${wechat.token.url}")
    private String tokenUrl;

    private String token;

    @Value("${wechat.message.url}")
    private String messageUrl;

    @Override
    public void sendMessage(WeChatMessage weChatMessage) {
        messageUrl = messageUrl.replaceAll("ACCESS_TOKEN", token);
        /**
         * url : 发送到那个url
         * Object object restTempalte 将该对象自动转换为json对象，使用jackson 转换
         * responseType: 返回值类型，restTempalte 发请求后得到一个byte ->String（json）->对象
         */
        String res = restTemplate.postForObject(messageUrl, weChatMessage, String.class);
        System.out.println(res+"------------------------------------------");
    }

    @Override
    @JmsListener(containerFactory = "containerFactory", concurrency = "3", destination = "weChat.message.queue")
    public void onMessage(Message message) {
        System.out.println(message+"-----");
        ObjectMessage objectMessage = (ObjectMessage) message;
        WeChatMessage weChatMessage = null;
        try {
            weChatMessage = (WeChatMessage) objectMessage.getObject();
            sendMessage(weChatMessage);
            //发送成功就签收一下
            message.acknowledge();
        } catch (Exception e) {
            //如果发送失败，就进这个里面来
            if (e instanceof JMSException) {
                //打印一下
                System.out.println(message);
            } else {
                int i = 3;
                while (i > 0) {
                    try {
                        //校验一下 测试发送三次 成功就签收 不成功就打印一下
                        sendMessage(weChatMessage);
                        break;
                    } catch (Exception e1) {
                        i--;
                    }
                    try {
                        message.acknowledge();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 因为token是2个小时过期 所以 这个时间快到了就刷新一下token
     */
    @Scheduled(fixedRate = 7000 * 1000)
    public void f5Token() {
        System.out.println(66666+"------------------------------------");
        tokenUrl = tokenUrl.replaceAll("APPID", appId).replaceAll("APPSECRET", secret);
        TokenResult tokenResult = restTemplate.getForObject(tokenUrl, TokenResult.class);
        token = tokenResult.getAccessToken();
        System.out.println(token+"----------------------------");
    }

}
