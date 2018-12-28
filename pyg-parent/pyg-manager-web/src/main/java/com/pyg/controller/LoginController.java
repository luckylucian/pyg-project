package com.pyg.controller;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


/**
 * 获取登录用户名，并返回
 */
@RestController
@RequestMapping("/login")
public class LoginController {


    @RequestMapping("/getLoginName")
    public Map getLoginName(){

        //security框架自动获取登录用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        //创建map集合，存入用户名
        Map map = new HashMap();
        map.put("loginName",name);
        return map;

    }
}
