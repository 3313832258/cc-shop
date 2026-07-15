package com.ccshop.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ccshop.promotion.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /** 原子递减 remaining_count，防超发 */
    @Update("UPDATE coupon SET remaining_count = remaining_count - 1 WHERE id = #{id} AND remaining_count > 0")
    int decrementRemaining(@Param("id") Long id);

    /** 递增回滚 */
    @Update("UPDATE coupon SET remaining_count = remaining_count + 1 WHERE id = #{id}")
    void incrementRemaining(@Param("id") Long id);
}
