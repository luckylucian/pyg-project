package com.pyg.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pyg.mapper.TbOrderItemMapper;
import com.pyg.mapper.TbPayLogMapper;
import com.pyg.pojo.*;
import com.pyg.pojoCombine.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbOrderMapper;
import com.pyg.pojo.TbOrderExample.Criteria;
import com.pyg.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private TbPayLogMapper payLogMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbOrder order) {
        //得到购物车数据
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

        List<String> orderList = new ArrayList<>();    //订单 ID 列表

        double total_money = 0;

        for (Cart cart : cartList) {
            System.out.println("sellerId:" + cart.getSellerId());
            long orderId = idWorker.nextId();
            TbOrder tbOrder = new TbOrder();
            tbOrder.setOrderId(orderId);
            tbOrder.setUserId(order.getUserId());
            tbOrder.setPaymentType(order.getPaymentType());
            tbOrder.setStatus("1");
            tbOrder.setCreateTime(new Date());
            tbOrder.setUpdateTime(new Date());
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());
            tbOrder.setReceiverMobile(order.getReceiverMobile());
            tbOrder.setReceiver(order.getReceiver());
            tbOrder.setSourceType(order.getSourceType());
            tbOrder.setSellerId(cart.getSellerId());    //sellerId由购物车获取

            //循环购物车明细
            double money = 0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {

                orderItem.setId(idWorker.nextId());
                orderItem.setOrderId(orderId);
                orderItem.setSellerId(cart.getSellerId());
                orderItemMapper.insert(orderItem);
                money += orderItem.getTotalFee().doubleValue();    //金额累加
            }
            tbOrder.setPayment(new BigDecimal(money));
            orderMapper.insert(tbOrder);

            orderList.add(orderId + "");    //添加到订单列表
            total_money += money;

        }

        if ("1".equals(order.getPaymentType())) {    //如果是微信支付
            TbPayLog payLog = new TbPayLog();
            String outTradeNo = idWorker.nextId() + "";
            payLog.setOutTradeNo(outTradeNo);
            payLog.setCreateTime(new Date());
            //订单号列表，逗号分隔
            String orderIds = orderList.toString().replace("[", "").replace("]", "");
            payLog.setOrderList(orderIds);
            payLog.setPayType("1");
            payLog.setTotalFee((long) (total_money*100));
            payLog.setTradeState("0");
            payLog.setUserId(order.getUserId());
            payLogMapper.insert(payLog);
            redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog); //支付日志放入缓存
        }

        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }


    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbOrder findOne(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            orderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbOrderExample example = new TbOrderExample();
        Criteria criteria = example.createCriteria();

        if (order != null) {
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
            }
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andPostFeeLike("%" + order.getPostFee() + "%");
            }
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andStatusLike("%" + order.getStatus() + "%");
            }
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andShippingNameLike("%" + order.getShippingName() + "%");
            }
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
            }
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + order.getUserId() + "%");
            }
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
            }
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
            }
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
            }
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
            }
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
            }
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
            }
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + order.getReceiver() + "%");
            }
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
            }
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
            }
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + order.getSellerId() + "%");
            }

        }

        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {

        //1.修改支付日志状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1");
        payLog.setTransactionId(transaction_id);
        payLogMapper.updateByPrimaryKey(payLog);

        //2.修改订单状态
        String orderList = payLog.getOrderList();
        String[] orderIds = orderList.split(",");
        for (String orderId : orderIds) {
            TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            if (order!=null){
                order.setStatus("2");   //已付款
                order.setPaymentTime(new Date());
                orderMapper.updateByPrimaryKey(order);
            }
        }

        //清除redis缓存数据
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());


    }

}
