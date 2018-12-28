package com.pyg.service.impl;

import com.pyg.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component("pageDeleteListener")
public class PageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {

        try {
            ObjectMessage objectMessage = (ObjectMessage) message;

            Long[] goodsIds = (Long[]) objectMessage.getObject();

            System.out.println("ItemDeleteListener 监听接收到消息..."+goodsIds);

            boolean b = itemPageService.deleteItemHtml(goodsIds);

            System.out.println("网页删除结果："+b);

        } catch (JMSException e) {

            e.printStackTrace();

        }
    }
}
