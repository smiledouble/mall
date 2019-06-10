package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.cxs.domain.Goods;
import com.cxs.domain.GoodsExample;
import com.cxs.mapper.GoodsMapper;
import com.cxs.service.GoodsTypeService;
import com.cxs.service.SolrImportService;
import com.github.pagehelper.PageHelper;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/29 22:23
 */
@Service(timeout = 7000)
public class SolrImportServiceImpl implements SolrImportService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private HttpSolrClient solrClient;

    @Autowired
    private GoodsTypeService goodsTypeService;

    /**
     * 一次性200条提交
     */
    private int rows = 200;
    /**
     * 给定开始时间 1970.1.1
     */
    private Date start = new Date(0);

    /**
     * 导入商品
     */
    @Override
    public long importAll() {
        //总条数
        long count = goodsMapper.countByExample(null);
        int totalPage = (int) (count % rows == 0 ? count / rows : (count / rows) + 1);
        Map<Integer, String> goodsTypes = this.goodsTypeService.findAllGoodsType();
        for (int i = 1; i <= totalPage; i++) {

            PageHelper.startPage(i, rows);
            List<Goods> goods = goodsMapper.selectByExample(null);
            List<SolrInputDocument> docs = new ArrayList<>();
            for (Goods g : goods) {
                docs.add(goods2Docs(g, goodsTypes));
            }
            try {
                solrClient.add(docs);
                solrClient.commit();
                System.out.println(i + "次导入成功");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(i + "次导入失败");
            }
        }
        return count;

    }

    /**
     * 15分钟导入一次
     *
     * @return
     */
    @Override
    @Scheduled(fixedRate = 60 * 15 * 1000)
    public long importAppend() {
        Date end = new Date(System.currentTimeMillis());

        //总条数
        GoodsExample example = new GoodsExample();
        example.createCriteria().andUpdatetimeBetween(start, end);
        long count = goodsMapper.countByExample(example);
        int totalPage = (int) (count % rows == 0 ? count / rows : (count / rows) + 1);
        Map<Integer, String> goodsTypes = this.goodsTypeService.findAllGoodsType();
        for (int i = 1; i <= totalPage; i++) {
            PageHelper.startPage(i, rows);
            List<Goods> goods = goodsMapper.selectByExample(null);
            List<SolrInputDocument> docs = new ArrayList<>();
            for (Goods g : goods) {
                docs.add(goods2Docs(g, goodsTypes));
            }
            try {
                solrClient.add(docs);
                solrClient.commit();
                System.out.println(i + "次导入成功");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(i + "次导入失败");
            }
        }
        start = end;
        return count;
    }

    /**
     * 将goods对象转成solr的导入对象
     *
     * @param g
     * @return
     */
    private SolrInputDocument goods2Docs(Goods g, Map<Integer, String> goodsTypes) {
        SolrInputDocument s = new SolrInputDocument();
        s.setField("id", g.getId().toString());
        s.setField("goods_name", g.getGoodsName());
        s.setField("goods_img", g.getOriginalImg() == null ? "" : g.getOriginalImg());
        s.setField("goods_price", g.getShopPrice() == null ? 0.00 : g.getShopPrice().doubleValue());
        s.setField("goods_comment_count", g.getCommentCount() == null ? "" : g.getCommentCount().longValue());
        s.setField("goods_type_name", goodsTypes.get(g.getCatId()));
        return s;
    }
}
