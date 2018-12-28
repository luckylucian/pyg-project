package com.pyg.service;


import java.util.Map;

/**
 * 微信支付接口
 */
public interface WechatPayService {

    public Map createNative(String out_trade_no,String total_fee);


    public Map queryPayStatus(String out_trade_no);

    public Map closePay(String out_trade_no);
}
