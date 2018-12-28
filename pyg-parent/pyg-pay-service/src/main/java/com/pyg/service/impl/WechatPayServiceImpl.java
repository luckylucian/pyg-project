package com.pyg.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pyg.service.WechatPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WechatPayServiceImpl implements WechatPayService {

    @Value("${appid}")
    private String appid;

    @Value("${mch_id}")
    private String mch_id; //商户号

    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1. 设置参数   参照开发文档中必填项
        Map<String,String> param = new HashMap();
        param.put("appid",appid);
        param.put("mch_id",mch_id);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body","品优购");
        param.put("out_trade_no",out_trade_no);
        param.put("total_fee",total_fee);
        param.put("spbill_create_ip","127.0.0.1");
        param.put("notify_url","http://www.sina.com");
        param.put("trade_type","NATIVE");

        try {
            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

            System.out.println(xmlParam);

            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            //3. 获取结果
            String result = client.getContent();

            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);

            System.out.println(resultMap.get("code_url"));  //打印二维码链接

            Map<String,String> map = new HashMap<>();
            map.put("code_url",resultMap.get("code_url"));  //支付二维码链接
            //map.put("code_url","https://www.baidu.com");  //支付二维码链接
            map.put("out_trade_no",out_trade_no);
            map.put("total_fee",total_fee);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        //1. 设置参数
        Map<String,String> param = new HashMap<>();
        param.put("appid",appid);
        param.put("mch_id",mch_id);
        param.put("out_trade_no",out_trade_no);
        param.put("nonce_str",WXPayUtil.generateNonceStr());

        try {
            //2. 生成要发送的xml  (工具类会自动根据随机字符串和算法生成并设置sign)
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            //3. 获取返回结果
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map closePay(String out_trade_no) {
        Map param=new HashMap();
        param.put("appid", appid);//公众账号 ID
        param.put("mch_id", mch_id);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url="https://api.mch.weixin.qq.com/pay/closeorder";

        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client=new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
