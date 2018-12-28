package com.pyg.service;

import com.pyg.pojoCombine.Cart;

import java.util.List;

public interface CartService {

    /**
     * 向购物车中添加商品
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList , Long itemId , Integer num);


    /**
     * 从缓存中获取购物车
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);


    /**
     * 向缓存中保存购物车
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username , List<Cart> cartList);

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList1 , List<Cart> cartList2);
}
