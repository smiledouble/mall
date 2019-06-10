package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.cxs.domain.Admin;
import com.cxs.domain.Cart;
import com.cxs.domain.CartExample;
import com.cxs.domain.Goods;
import com.cxs.mapper.CartMapper;
import com.cxs.model.SearchGoodsResult;
import com.cxs.model.SearchGoodsVo;
import com.cxs.redis.RedisClient;
import com.cxs.service.CartService;
import com.cxs.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/2 11:44
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisClient redis;

    @Autowired
    private CartMapper cartMapper;

    @Reference
    private GoodsService goodsService;


    /**
     * 添加到购物车的接口
     *
     * @param goodsId
     * @param goodsNum
     * @param label
     */
    @Override
    public void addCartUseRedis(Integer goodsId, Integer goodsNum, String label, String token) {
        //进来先判断用户有没有登录 如果登陆了就 将之前在redis的数据转移到数据库
        Admin admin = getAdminByToken(token);
        if (admin != null) {
            //后面添加的时候往数据库添加
            addGoodsToDB(goodsId, goodsNum, admin.getId().intValue());
        } else {
            addGoodsToRedis(label, goodsId, goodsNum);
        }
    }

    /**
     * 将商品添加到redis
     *
     * @param label
     * @param goodsId
     * @param goodsNum
     */
    private void addGoodsToRedis(String label, Integer goodsId, Integer goodsNum) {
        String goodsList = label + "GOODSLIST";
        String goodsInfo = label + "GOODSINFO";
        String goodsScore = label + "GOODSSCORE";
        //自增生成排序
        Long socre = redis.incre(goodsScore);
        //存放到zset里面
        redis.zadd(goodsList, socre, goodsId + "");
        long goodsLastNum = 0L;
        if (redis.hashExit(goodsInfo, goodsId + "")) {
            //根据key和id得到数量
            goodsLastNum = Long.valueOf(redis.getHash(goodsInfo, goodsId + ""));
        }
        //放到集合中
        redis.hset(goodsInfo, goodsId + "", (goodsLastNum + goodsNum) + "");
    }

    /**
     * 将商品添加到数据库
     *
     * @param goodsId
     * @param goodsNum
     * @param userId
     */
    private void addGoodsToDB(Integer goodsId, Integer goodsNum, Integer userId) {
        //先看数据库有没有想用的商品 如果有就修改数量 如果没有就添加
        CartExample example = new CartExample();
        example.createCriteria().andGoodsIdEqualTo(goodsId)
                .andUserIdEqualTo(userId);
        List<Cart> carts = cartMapper.selectByExample(example);
        if (carts.size() > 1) {
            //说明数据库有问题啊
            throw new RuntimeException("数据库数据节点错误");
        }
        if (carts.size() == 0) {
            //说明数据库没有，就直接添加了
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(goodsId);
            cart.setNum(goodsNum);
            cartMapper.insertSelective(cart);
            return;
        }
        //说明有 那么更新就好了
        Cart cart = carts.get(0);
        cart.setNum(cart.getNum() + goodsNum);
        cartMapper.updateByPrimaryKeySelective(cart);
        return;

    }


    /**
     * 获取总条数
     * 利用消息队列 就不需要转移数据了
     *
     * @param label
     * @return
     */
    @Override
    public Long getTotal(String label, String token) {
        Long total = 0L;
        String goodsInfo = label + "GOODSINFO";
        Admin admin = getAdminByToken(token);
        if (admin == null) {
            //没登录就从redis里面获取
            Map<String, String> goods_info = redis.hgetAll(goodsInfo);
            if (goods_info != null && goods_info.size() > 0) {
                Set<String> strings = goods_info.keySet();
                for (String k : strings) {
                    total += Long.valueOf(goods_info.get(k));
                }
            }
            return total;
        }
        //登陆了就从数据库获取
        String adminJson = redis.get(token);
        Admin admin1 = JSON.parseObject(adminJson, Admin.class);

        total = cartMapper.getTotalByUserId(admin1.getId().intValue());
        System.out.println(total);

        return total;
    }

    /**
     * 展示购物车的商品
     *
     * @param token
     * @return
     */
    @Override
    public SearchGoodsResult showCart(String label, String token) {
        String goodsList = label + "GOODSLIST";
        SearchGoodsResult searchGoodsResult = new SearchGoodsResult();
        String goodsInfo = label + "GOODSINFO";
        Admin admin = getAdminByToken(token);
        List<Integer> goodsIds = new ArrayList<>();
        List<SearchGoodsVo> searchGoodsVos = new ArrayList<>();
        if (admin == null && redis.isExsit(goodsInfo) && redis.isExsit(goodsList)) {
            //从redis里面获取商品列表
            Map<String, String> allGoods = redis.hgetAll(goodsInfo);

            Set<String> keySet = allGoods.keySet();
            keySet.forEach((k) -> goodsIds.add(Integer.valueOf(k)));
            //得到所有的商品信息
            List<Goods> goodsLists = this.goodsService.findGoodsByIds(goodsIds);
            goodsLists.forEach((goods) -> {
                SearchGoodsVo searchGoodsVo = goods2SearchGoodsVo(goods);
                searchGoodsVo.setSize(Integer.valueOf(allGoods.get(goods.getId().toString()) + ""));
                searchGoodsVos.add(searchGoodsVo);
            });
            searchGoodsResult.setResults(searchGoodsVos);
            return searchGoodsResult;
        }
        //从数据库里面查询获取
        //1从cart表里面查询用户id为 admin.getId()的用户的数据
        CartExample example = new CartExample();
        example.createCriteria().andUserIdEqualTo(admin.getId().intValue());
        List<Cart> carts = this.cartMapper.selectByExample(example);
        carts.forEach((cart) -> goodsIds.add(cart.getGoodsId()));
        //根据商品的id查询出所有的商品对象
        List<Goods> goodsByIds = this.goodsService.findGoodsByIds(goodsIds);
        goodsByIds.forEach((goods) -> {
            SearchGoodsVo searchGoodsVo = goods2SearchGoodsVo(goods);
            carts.forEach((cart) -> {
                if (goods.getId().equals(cart.getGoodsId())) {
                    searchGoodsVo.setSize(cart.getNum());
                }
            });
            searchGoodsVos.add(searchGoodsVo);
        });
        searchGoodsResult.setResults(searchGoodsVos);
        long total = this.cartMapper.getTotalByUserId(admin.getId().intValue());
        searchGoodsResult.setTotal(total);
        return searchGoodsResult;

    }

    /**
     * 清空预下单的购物车
     *
     * @param goodsInfo
     * @param userId
     */
    @Override
    public void cleanCart(Map<Integer, Integer> goodsInfo, int userId) {
        List<Integer> list = new ArrayList<>();
        Set<Integer> integers = goodsInfo.keySet();
        for (Integer i : integers) {
            list.add(i);
        }
        CartExample example = new CartExample();
        example.createCriteria().andUserIdEqualTo(userId).andGoodsIdIn(list);
        cartMapper.deleteByExample(example);

    }

    /**
     * 补偿购物车
     *
     * @param goodsInfo
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void compensateCart(Map<Integer, Integer> goodsInfo, int userId) {
        goodsInfo.forEach((goodsId, goodsNum) -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(goodsId);
            cart.setNum(goodsNum);
            cartMapper.insert(cart);
        });

    }


    private SearchGoodsVo goods2SearchGoodsVo(Goods goods) {
        SearchGoodsVo searchGoodsVo = new SearchGoodsVo();
        searchGoodsVo.setId(goods.getId().toString());
        searchGoodsVo.setGoodsNameHl(goods.getGoodsName());
        searchGoodsVo.setGoodsPrice(goods.getShopPrice().toString());
        searchGoodsVo.setGoodsImg(goods.getOriginalImg());
        return searchGoodsVo;
    }


    /**
     * 根据redis 的key 获取用户对象
     *
     * @param token
     * @return
     */
    public Admin getAdminByToken(String token) {
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

}
