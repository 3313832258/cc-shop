package com.ccshop.promotion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.BusinessException;
import com.ccshop.common.core.Result;
import com.ccshop.promotion.dto.FlashSaleVO;
import com.ccshop.promotion.entity.FlashSaleActivity;
import com.ccshop.promotion.entity.FlashSaleItem;
import com.ccshop.promotion.feign.ProductClient;
import com.ccshop.promotion.mapper.FlashSaleActivityMapper;
import com.ccshop.promotion.mapper.FlashSaleItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final FlashSaleActivityMapper activityMapper;
    private final FlashSaleItemMapper itemMapper;
    private final StringRedisTemplate redisTemplate;
    private final ProductClient productClient;
    private final DistributedLockService distributedLockService;

    private DefaultRedisScript<Long> flashSaleScript;

    private static final String STOCK_KEY_PREFIX = "flash:stock:";
    private static final String LIMIT_KEY_PREFIX = "flash:limit:";

    @PostConstruct
    public void init() {
        flashSaleScript = new DefaultRedisScript<>();
        flashSaleScript.setLocation(new ClassPathResource("lua/flash_sale.lua"));
        flashSaleScript.setResultType(Long.class);
    }

    /**
     * 获取秒杀商品列表
     */
    public List<FlashSaleVO> getFlashSaleItems() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSaleActivity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<FlashSaleActivity>()
                        .le(FlashSaleActivity::getStartTime, now.plusHours(1))
                        .ge(FlashSaleActivity::getEndTime, now));

        List<FlashSaleVO> vos = new ArrayList<>();
        for (FlashSaleActivity activity : activities) {
            List<FlashSaleItem> items = itemMapper.selectList(
                    new LambdaQueryWrapper<FlashSaleItem>()
                            .eq(FlashSaleItem::getActivityId, activity.getId()));

            for (FlashSaleItem item : items) {
                FlashSaleVO vo = new FlashSaleVO();
                vo.setId(item.getId());
                vo.setActivityId(activity.getId());
                vo.setProductId(item.getProductId());
                vo.setSkuId(item.getSkuId());
                vo.setFlashPrice(item.getFlashPrice());
                vo.setTotalStock(item.getTotalStock());
                vo.setAvailableStock(item.getAvailableStock());
                vo.setLimitPerUser(item.getLimitPerUser());
                vo.setStartTime(activity.getStartTime());
                vo.setEndTime(activity.getEndTime());

                // 计算活动状态
                if (now.isBefore(activity.getStartTime())) {
                    vo.setActivityStatus(0); // 未开始
                } else if (now.isAfter(activity.getEndTime())) {
                    vo.setActivityStatus(2); // 已结束
                } else {
                    vo.setActivityStatus(1); // 进行中
                }

                // 获取商品信息
                try {
                    Result<ProductClient.SkuVO> skuResult = productClient.getSku(item.getSkuId());
                    if (skuResult != null && skuResult.getCode() == 200 && skuResult.getData() != null) {
                        ProductClient.SkuVO sku = skuResult.getData();
                        vo.setProductName(sku.getProductName());
                        vo.setProductImage(sku.getImage());
                        vo.setOriginalPrice(sku.getPrice());
                    }
                } catch (Exception e) {
                    log.warn("获取商品信息失败: skuId={}", item.getSkuId(), e);
                }

                vos.add(vo);
            }
        }
        return vos;
    }

    /**
     * 预热秒杀库存到 Redis
     */
    public void warmUpStock(Long itemId) {
        FlashSaleItem item = itemMapper.selectById(itemId);
        if (item == null) return;

        String stockKey = STOCK_KEY_PREFIX + itemId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(item.getAvailableStock()));
        log.info("预热秒杀库存: itemId={}, stock={}", itemId, item.getAvailableStock());
    }

    /**
     * 秒杀抢购（带分布式锁）
     */
    public boolean flashSale(Long userId, Long itemId) {
        FlashSaleItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(400, "秒杀商品不存在");
        }

        // 检查活动状态
        FlashSaleActivity activity = activityMapper.selectById(item.getActivityId());
        if (activity == null) {
            throw new BusinessException(400, "秒杀活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new BusinessException(400, "秒杀活动未开始");
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException(400, "秒杀活动已结束");
        }

        // 分布式锁 Key，按商品维度加锁，保证同一商品的秒杀串行执行
        String lockKey = "flash:sale:" + itemId;
        String lockValue = null;

        try {
            // 尝试获取分布式锁，最多等待 3 秒，锁超时 10 秒
            lockValue = distributedLockService.tryLock(lockKey, 10);
            if (lockValue == null) {
                throw new BusinessException(429, "系统繁忙，请稍后重试");
            }

            // 执行 Lua 脚本
            String stockKey = STOCK_KEY_PREFIX + itemId;
            String limitKey = LIMIT_KEY_PREFIX + itemId + ":" + userId;

            Long result = redisTemplate.execute(flashSaleScript,
                    List.of(stockKey, limitKey),
                    String.valueOf(userId),
                    String.valueOf(item.getLimitPerUser()));

            if (result == null) {
                throw new BusinessException(500, "秒杀失败，请重试");
            }

            switch (result.intValue()) {
                case 1:
                    // 成功，异步落库
                    log.info("秒杀成功: userId={}, itemId={}", userId, itemId);
                    return true;
                case 0:
                    throw new BusinessException(400, "库存不足");
                case -1:
                    throw new BusinessException(400, "库存未初始化");
                case -2:
                    throw new BusinessException(400, "超过限购数量");
                default:
                    throw new BusinessException(500, "秒杀失败");
            }
        } finally {
            // 释放分布式锁
            if (lockValue != null) {
                distributedLockService.unlock(lockKey, lockValue);
            }
        }
    }
}
