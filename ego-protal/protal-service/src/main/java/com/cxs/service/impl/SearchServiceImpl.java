package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.cxs.domain.Goods;
import com.cxs.model.PageBean;
import com.cxs.model.SearchGoodsResult;
import com.cxs.model.SearchGoodsVo;
import com.cxs.service.GoodsService;
import com.cxs.service.SearchService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/29 16:55
 */
@Service(timeout = 7000)
public class SearchServiceImpl implements SearchService {

    @Autowired
    private HttpSolrClient solrClient;

    @Reference
    private GoodsService goodsService;


    @Override
    public SearchGoodsResult doSearch(String keywords, int page, int rows, boolean price, boolean comment, boolean commmon, Integer min, Integer max) {
        if (keywords == null || "".equals(keywords)) {
            throw new RuntimeException("查询时关键字不能为空");
        }
        SolrQuery solrQuery = new SolrQuery("keywords:" + keywords);

        // 设置区间查询
        if (min != null && max != null) {
            solrQuery.setFilterQueries("goods_price:[" + min + " TO " + max + "]");
        }
        solrQuery.setStart((page - 1) * rows);
        solrQuery.setRows(rows);
        if (price) {
            solrQuery.addSort("goods_price", SolrQuery.ORDER.asc);
        }

        // 评论数排序
        if (comment) {
            solrQuery.addSort("goods_comment_count", SolrQuery.ORDER.desc);
        }

        solrQuery.setHighlight(true);
        solrQuery.setHighlightSimplePre("<font color = 'red'>");
        solrQuery.setHighlightSimplePost("</font>");
        solrQuery.addHighlightField("goods_name");
        SearchGoodsResult searchGoodsResult = new SearchGoodsResult();
        List<SearchGoodsVo> goodsVoList = new ArrayList<SearchGoodsVo>();
        try {
            //查询
            QueryResponse query = solrClient.query(solrQuery);
            SolrDocumentList results = query.getResults();
            //获得高亮的集合
            Map<String, Map<String, List<String>>> highlighting = query.getHighlighting();
            for (SolrDocument solrDocument : results) {
                SearchGoodsVo searchGoodsVo = doc2GoodsVo(solrDocument);
                String goods_name = null;
                if (highlighting != null && highlighting.get(searchGoodsVo.getId()).size() > 0) {
                    goods_name = highlighting.get(searchGoodsVo.getId()).get("goods_name").get(0);
                } else {
                    goods_name = solrDocument.getFieldValue("goods_name").toString();
                }
                searchGoodsVo.setGoodsNameHl(goods_name);
                goodsVoList.add(searchGoodsVo);
            }
            searchGoodsResult.setPage(page);
            searchGoodsResult.setTotal(results.getNumFound());
            searchGoodsResult.setResults(goodsVoList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("solr调用失败");
        }
        return searchGoodsResult;
    }

    /**
     * 根据CatId在数据库搜索
     *
     * @param catId
     * @param page
     * @param rows
     * @param price
     * @param comment
     * @param common
     * @param min
     * @param max
     * @return
     */
    @Override
    public SearchGoodsResult doQuery(Integer catId, Integer page, Integer rows, boolean price, boolean comment, boolean common, Integer min, Integer max) {
        PageBean<Goods> goodsPageBean = this.goodsService.selectGoods(catId, page, rows, price, comment, common, min, max);
        SearchGoodsResult searchGoodsResult = new SearchGoodsResult();
        searchGoodsResult.setPage(page);
        searchGoodsResult.setTotal(goodsPageBean.getTotal());
        List<Goods> data = goodsPageBean.getData();
        List<SearchGoodsVo> goodsVos = new ArrayList<SearchGoodsVo>();
        data.forEach((goods) -> {
            goodsVos.add(goods2SearchGoodsVo(goods));
        });
        searchGoodsResult.setResults(goodsVos);

        return searchGoodsResult;
    }

    /**
     * 把goods对象转成指定的vo对象
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

    /**
     * 查询的文档转vo对象
     *
     * @param solrDocument
     * @return
     */
    private SearchGoodsVo doc2GoodsVo(SolrDocument solrDocument) {
        SearchGoodsVo searchGoodsVo = new SearchGoodsVo();
        searchGoodsVo.setId(solrDocument.getFieldValue("id").toString());
        searchGoodsVo.setGoodsImg(solrDocument.getFieldValue("goods_img").toString());
        searchGoodsVo.setGoodsPrice(solrDocument.getFieldValue("goods_price").toString());
        return searchGoodsVo;
    }
}
