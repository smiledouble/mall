package com.cxs.listener;

import com.alibaba.fastjson.JSON;
import com.cxs.domain.Admin;
import com.cxs.domain.Cart;
import com.cxs.domain.CartExample;
import com.cxs.mapper.CartMapper;
import com.cxs.redis.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/3 21:24
 */
@Component("loginQueueListener")
public class LoginQueueListener implements MessageListener {

    @Autowired
    private RedisClient redis;
    @Autowired
    private CartMapper cartMapper;

    /**
     * 监听消息队列的消息
     * 注解开发的形式
     *
     * @param message
     */
    @Override
    @JmsListener(concurrency = "3-5", containerFactory = "containerFactory", destination = "user.login.queue")
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        String token = null;
        String label = null;
        try {
            String text = textMessage.getText();
            label = text.split(",")[0];
            token = text.split(",")[1];
        } catch (JMSException e) {
            e.printStackTrace();
        }
        //监听到用户登录以后就将数据转移到数据库
        Admin admin = getAdminByToken(token);
        try {
            assert admin != null;
            transDataFromRedisToDB(label, admin.getId().intValue());
            message.acknowledge();//客户端签收
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 根据redis 的key 获取用户对象
     *
     * @param token
     * @return
     */
    private Admin getAdminByToken(String token) {
        if (token == null || "".equals(token)) {
            return null;
        }
        if (!redis.isExsit(token)) {
            return null;
        }
        String adminJson = redis.get(token);
        Admin admin = JSON.parseObject(adminJson, Admin.class);
        return admin;
    }


    /**
     * redis数据转移到db
     *
     * @param label
     * @param userId
     */
    private void transDataFromRedisToDB(String label, Integer userId) {
        //先去查redis
        String goodsInfo = label + "GOODSINFO";
        Map<String, String> allRedisGoods = redis.hgetAll(goodsInfo);
        if (allRedisGoods == null || allRedisGoods.size() == 0) {
            return;
        }
        //将map的key转成integer的list
        Set<String> goodsKeys = allRedisGoods.keySet();
        List<Integer> goodsIds = new ArrayList<>();
        //获得所有redis里面的商品id
        goodsKeys.forEach((goodsId) -> goodsIds.add(Integer.valueOf(goodsId + "")));

        CartExample example = new CartExample();
        example.createCriteria().andGoodsIdIn(goodsIds).andUserIdEqualTo(userId);
        //查询出redis和db的交集
        List<Cart> carts = cartMapper.selectByExample(example);
        carts.forEach((cart) -> {
            cart.setNum(cart.getNum() + Integer.valueOf(allRedisGoods.get(cart.getGoodsId() + "")));
            cartMapper.updateByPrimaryKeySelective(cart);
            //移除掉 去差集
            goodsIds.remove(cart.getGoodsId());
        });
        //数据库没有的就直接插入
        goodsIds.forEach((goodsId) -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(goodsId);
            cart.setNum(Integer.valueOf(allRedisGoods.get(goodsId + "")));
            cartMapper.insertSelective(cart);
        });
        //清空redis里面的数据
        String goodsList = label + "GOODSLIST";
        String goodsScore = label + "GOODSSCORE";
        redis.del(goodsInfo, goodsList, goodsScore);

    }
}
