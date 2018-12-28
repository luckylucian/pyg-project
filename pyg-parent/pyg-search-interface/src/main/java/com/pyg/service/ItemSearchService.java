package com.pyg.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);

    public void importList(List list);


    public void deleteByGoodsIds(List goodsIds);

}
