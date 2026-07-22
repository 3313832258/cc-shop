package com.ccshop.promotion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.BusinessException;
import com.ccshop.common.core.Constants;
import com.ccshop.promotion.dto.CouponVO;
import com.ccshop.promotion.dto.MyCouponVO;
import com.ccshop.promotion.entity.Coupon;
import com.ccshop.promotion.entity.UserCoupon;
import com.ccshop.promotion.mapper.CouponMapper;
import com.ccshop.promotion.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ccshop.common.enums.CouponStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final StringRedisTemplate redisTemplate;

    public List<CouponVO> getAvailable(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Coupon> qw = new LambdaQueryWrapper<Coupon>()
                .gt(Coupon::getRemainingCount, 0)
                .le(Coupon::getStartTime, now)
                .ge(Coupon::getEndTime, now)
                .orderByAsc(Coupon::getValue);
        List<Coupon> list = couponMapper.selectList(qw);
        List<CouponVO> vos = new ArrayList<>();
        for (Coupon c : list) {
            CouponVO vo = toCouponVO(c);
            // 查询用户是否已领取该优惠券
            if (userId != null) {
                Long count = userCouponMapper.selectCount(
                        new LambdaQueryWrapper<UserCoupon>()
                                .eq(UserCoupon::getUserId, userId)
                                .eq(UserCoupon::getCouponId, c.getId()));
                vo.setReceived(count > 0);
            } else {
                vo.setReceived(false);
            }
            vos.add(vo);
        }
        return vos;
    }

    @Transactional
    public void receive(Long userId, Long couponId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 1. 检查是否已领取
        Long count = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId));
        if (count > 0) {
            throw new BusinessException(400, "已领取过该优惠券");
        }

        // 2. Redis 原子预扣
        String redisKey = Constants.COUPON_REMAINING + couponId;
        Long remaining = redisTemplate.opsForValue().decrement(redisKey);

        if (remaining == null || remaining < 0) {
            // 回滚 Redis
            if (remaining != null && remaining < 0) {
                redisTemplate.opsForValue().increment(redisKey);
            }
            // 从 DB 加载余量到 Redis
            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null) {
                throw new BusinessException(404, "优惠券不存在");
            }
            if (coupon.getRemainingCount() <= 0) {
                throw new BusinessException(400, "优惠券已领完");
            }
            redisTemplate.opsForValue().set(redisKey, String.valueOf(coupon.getRemainingCount()));
            throw new BusinessException(400, "优惠券已领完");
        }

        // 3. DB 同步扣减
        int updated = couponMapper.decrementRemaining(couponId);
        if (updated == 0) {
            // DB 扣减失败，回滚 Redis
            redisTemplate.opsForValue().increment(redisKey);
            throw new BusinessException(400, "优惠券已领完");
        }

        // 4. 插入用户优惠券记录
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(0);
        userCouponMapper.insert(uc);
    }

    public List<MyCouponVO> getMyCoupons(Long userId, Integer status) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        LambdaQueryWrapper<UserCoupon> qw = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getCreatedAt);

        if (status != null) {
            qw.eq(UserCoupon::getStatus, status);
        }

        List<UserCoupon> list = userCouponMapper.selectList(qw);
        List<MyCouponVO> vos = new ArrayList<>();
        for (UserCoupon uc : list) {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            MyCouponVO vo = new MyCouponVO();
            vo.setId(uc.getId());
            vo.setCouponId(uc.getCouponId());
            vo.setName(coupon != null ? coupon.getName() : "已下架");
            vo.setType(coupon != null ? coupon.getType() : 0);
            vo.setValue(coupon != null ? coupon.getValue() : null);
            vo.setMinOrderAmount(coupon != null ? coupon.getMinOrderAmount() : null);
            vo.setEndTime(coupon != null ? coupon.getEndTime() : null);
            vo.setStatus(uc.getStatus());
            vo.setCreatedAt(uc.getCreatedAt());
            vos.add(vo);
        }
        return vos;
    }

    private CouponVO toCouponVO(Coupon c) {
        CouponVO vo = new CouponVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setType(c.getType());
        vo.setValue(c.getValue());
        vo.setMinOrderAmount(c.getMinOrderAmount());
        vo.setStartTime(c.getStartTime());
        vo.setEndTime(c.getEndTime());
        vo.setTotalCount(c.getTotalCount());
        vo.setRemainingCount(c.getRemainingCount());
        return vo;
    }

    /** 核销优惠券（MQ消费者调用） */
    @Transactional
    public void useCoupon(Long userId, Long userCouponId, Long orderId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null || !uc.getUserId().equals(userId)) {
            throw new BusinessException(400, "优惠券不存在");
        }
        if (uc.getStatus() != CouponStatus.AVAILABLE.getCode()) {
            throw new BusinessException(400, "优惠券不可用");
        }
        uc.setStatus(CouponStatus.USED.getCode());
        uc.setUsedOrderId(orderId);
        userCouponMapper.updateById(uc);
    }

    /** 恢复优惠券（超时取消时调用） */
    @Transactional
    public void restoreCoupon(Long userId, Long userCouponId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc != null && uc.getUserId().equals(userId) && uc.getStatus() == CouponStatus.USED.getCode()) {
            uc.setStatus(CouponStatus.AVAILABLE.getCode());
            uc.setUsedOrderId(null);
            userCouponMapper.updateById(uc);
        }
    }

    /** 查询券详情（供交易服务 Feign 调用） */
    public CouponInfoDTO getCouponDetail(Long userCouponId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null) {
            throw new BusinessException(404, "优惠券不存在");
        }
        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        if (coupon == null) {
            throw new BusinessException(404, "优惠券模板不存在");
        }
        CouponInfoDTO dto = new CouponInfoDTO();
        dto.setUserCouponId(uc.getId());
        dto.setUserId(uc.getUserId());
        dto.setType(coupon.getType());
        dto.setValue(coupon.getValue());
        dto.setMinOrderAmount(coupon.getMinOrderAmount());
        return dto;
    }

    @Data
    public static class CouponInfoDTO {
        private Long userCouponId;
        private Long userId;
        private Integer type;
        private BigDecimal value;
        private BigDecimal minOrderAmount;
    }
}
