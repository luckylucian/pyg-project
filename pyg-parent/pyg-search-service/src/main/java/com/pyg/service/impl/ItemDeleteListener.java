package com.pyg.service.impl;

import com.pyg.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            System.out.println("ItemDeleteListener 监听接收到消息..."+goodsIds);

            itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
            System.out.println("成功删除索引库中的记录");

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
