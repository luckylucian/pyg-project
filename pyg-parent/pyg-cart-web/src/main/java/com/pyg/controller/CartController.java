package com.pyg.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.pojoCombine.Cart;
import com.pyg.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 9000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){

        String username = SecurityContextHolder.getContext().getAuthentication().getName(); //获取登录名
        System.out.println(username);

        String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")){
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);

        if (username.equals("anonymousUser")){  //未登录
            System.out.println("从cookie中获取购物车");
            return cartList_cookie;

        }else { //已登录
            List<Cart> cartList_Redis = cartService.findCartListFromRedis(username);
            System.out.println("从Redis中获取购物车");

            if(cartList_cookie.size()>0){   //如果本地存在购物车
                //合并购物车
                cartList_Redis = cartService.mergeCartList(cartList_Redis, cartList_cookie);
                //清除本地 cookie 的数据
                util.CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入 redis
                cartService.saveCartListToRedis(username, cartList_Redis);
            }
            return cartList_Redis;
        }

    }

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId , Integer num){
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //response.setHeader("Access-Control-Allow-Credentials", "true");

        String username = SecurityContextHolder.getContext().getAuthentication().getName(); //获取登录名
        System.out.println("当前登录用户："+username);

        try {
            List<Cart> cartList = findCartList();   //获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            if (username.equals("anonymousUser")){  //未登录
                util.CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*24,"UTF-8");
                System.out.println("向 cookie 存入数据");
            }else {   //已登录
                cartService.saveCartListToRedis(username,cartList);
                System.out.println("向 Redis 存入数据");
            }
            return new Result(true,"添加成功");

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }



}
