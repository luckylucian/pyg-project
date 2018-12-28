package com.pyg.service;

/**
 * 商品详情页接口
 */
public interface ItemPageService {

    public boolean genItemHtml(Long goodsId);

    public boolean deleteItemHtml(Long[] goodsIds);
}
