package com.pyg.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbContent;
import com.pyg.service.ContentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference  //注意导的包
    private ContentService contentService;

    @RequestMapping("/findByCategoryId")
    public List<TbContent> findByCategoryId(Long categoryId){
        return contentService.findByCategoryId(categoryId);
    }


}
