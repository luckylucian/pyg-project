package com.pyg.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbPayLog;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.service.SeckillOrderService;
import com.pyg.service.WechatPayService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 9000)
    private WechatPayService wechatPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder seckillOrder = seckillOrderService.searchSeckillOrderFromRedisByUserId(userId);

        //判断秒杀订单存在
        if (seckillOrder!=null){
            return wechatPayService.createNative(seckillOrder.getId() +"", (long)(seckillOrder.getMoney().doubleValue()*100)+"");
        }else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        //获取当前用户
        String userId=SecurityContextHolder.getContext().getAuthentication().getName();

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
                //将订单存入数据库
                seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
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

                //1.调用微信的关闭订单接口
                Map<String,String> payresult = wechatPayService.closePay(out_trade_no);
                if( !"SUCCESS".equals(payresult.get("result_code")) ){  //如果返回结果是非正常关闭
                    if("ORDERPAID".equals(payresult.get("err_code"))){
                        result=new Result(true, "支付成功");
                        seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
                    }
                }
                if(result.isSuccess()==false){
                    System.out.println("超时，取消订单");
                    //2.调用删除
                    seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return result;
    }
}
