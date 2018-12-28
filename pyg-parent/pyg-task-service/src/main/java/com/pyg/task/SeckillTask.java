package com.pyg.task;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 刷新秒杀商品
     */
    @Scheduled(cron="* * * * * ?")
    public void refreshSeckillGoods(){
        System.out.println("执行了秒杀商品增量更新 任务调度"+new Date());

        //查询缓存中的秒杀商品ID集合
        List goodsIdList = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        System.out.println(goodsIdList);

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        criteria.andStockCountGreaterThan(0);//剩余库存大于 0
        criteria.andStartTimeLessThanOrEqualTo(new Date());
        criteria.andEndTimeGreaterThan(new Date());

        if (goodsIdList.size() > 0){
            criteria.andIdNotIn(goodsIdList);   //排除缓存中已经存在的商品 ID 集合
        }

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

        //数据库查询结果存入缓存
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
            System.out.println("增量更新秒杀商品ID：" + seckillGoods.getId());
        }
        System.out.println("-----end-----");
    }
}
