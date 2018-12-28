package com.pyg.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbItem;
import com.pyg.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String, Object> map = new HashMap();

        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));

        //1.查询列表
        map.putAll(searchList(searchMap));
        //2.分组查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);

        //3.查询品牌和规格列表
        String categoryName = (String) searchMap.get("category");
        if (!"".equals(categoryName)) {//如果有分类名称
            map.putAll(searchBrandAndSpecList(categoryName));
        } else {  //如果没有分类名称，按照第一个查询
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();

    }

    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //查询列表方法
    public Map searchList(Map searchMap) {
        Map map = new HashMap();

        //高亮选项初始化
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);    //为查询对象设置高亮选项

        //1.1 关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2 按照商品分类过滤
        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3 按照品牌过滤
        if (!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4 过滤规格
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.5 按价格筛选
        if (!"".equals(searchMap.get("price"))) {
            String[] price = ((String) searchMap.get("price")).split("-");
            if (!price[0].equals("0")) {  //如果区间起点不等于 0
                Criteria filterCriteria = new
                        Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")) {//如果区间终点不等于*
                Criteria filterCriteria = new
                        Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }

        //1.6 分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo"); //提取页码
        if (pageNo == null) {
            pageNo = 1; //默认第一页
        }

        Integer pageSize=(Integer) searchMap.get("pageSize");//每页记录数
        if(pageSize==null){
            pageSize=20;//默认 20
        }

        query.setOffset((pageNo-1)* pageSize);  //查询起始索引
        query.setRows(pageSize);

        //1.7 排序
        String sortValue = (String) searchMap.get("sort");  //ASC DESC
        String sortField = (String) searchMap.get("sortField"); //排序字段
        if (sortValue != null && !sortValue.equals("")){
            if(sortValue.equals("ASC")){
                Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
                query.addSort(sort);
            }
            if(sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }




        //*********** 获取高亮结果集 **********
        //高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //高亮入口集合
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList) {
            //获取高亮列表
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();
            /*for (HighlightEntry.Highlight h : highlightList) {
                List<String> sns = h.getSnipplets();
                System.out.println(sns);
            }*/
            if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {
                TbItem item = entry.getEntity();
                item.setTitle(highlightList.get(0).getSnipplets().get(0));
            }

        }

        map.put("rows", page.getContent());

        map.put("totalPages",page.getTotalPages()); //返回总页数
        map.put("total",page.getTotalElements());   //返回总记录数

        return map;
    }

    //分组查询（查询商品分类列表）
    public List<String> searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList();

        Query query = new SimpleQuery("*:*");
        //相当于where条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");    //group by 
        query.setGroupOptions(groupOptions);
        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");

        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();

        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue()); //将分组结果添加到集合中
        }
        return list;
    }


    @Autowired
    private RedisTemplate redisTemplate;

    //查询品牌和规格列表
    private Map searchBrandAndSpecList(String category) {

        Map map = new HashMap();

        //1.根据商品分类名称得到模板ID
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        if (templateId != null) {
            //2.根据模板ID , 获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);

            //3.根据模板ID ， 获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }

        return map;
    }
}
