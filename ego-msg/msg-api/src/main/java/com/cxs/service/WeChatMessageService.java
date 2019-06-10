package com.cxs.service;

import com.cxs.model.WeChatMessage;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/4 13:23
 */
public interface WeChatMessageService {

    void sendMessage(WeChatMessage weChatMessage);

}
