package com.pyg.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbPayLog;
import com.pyg.service.OrderService;
import com.pyg.service.WechatPayService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 9000)
    private WechatPayService wechatPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.searchPayLogFromRedis(username);
        if (payLog!=null){
            return wechatPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
        }else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        int x = 0;
        while (true){
            Map<String,String> map = wechatPayService.queryPayStatus(out_trade_no);
            if (map==null){
                result = new Result(false,"支付出错");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")){
                result = new Result(true,"支付成功");
                //修改订单状态
                orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            x++;
            if (x >= 100){
                result = new Result(false,"二维码超时");
                break;
            }
        }
        return result;
    }
}
