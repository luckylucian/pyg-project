package com.pyg.service.impl;


import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ItemSearchListener implements MessageListener{

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        System.out.println("监听接收到消息...");

        try {
            TextMessage textMessage = (TextMessage) message;

            String text = textMessage.getText();

            System.out.println("监听接收到消息..." +text);

            List<TbItem> list = JSON.parseArray(text, TbItem.class);

            itemSearchService.importList(list);

            System.out.println("成功导入到索引库");

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
