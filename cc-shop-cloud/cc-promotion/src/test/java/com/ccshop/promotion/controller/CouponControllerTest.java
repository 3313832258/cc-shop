package com.ccshop.promotion.controller;

import com.ccshop.promotion.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@DisplayName("优惠券控制器测试")
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("获取可领取优惠券列表")
    void testGetAvailableCoupons() throws Exception {
        mockMvc.perform(get("/promotion/coupon/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("领取优惠券成功")
    void testReceiveCouponSuccess() throws Exception {
        mockMvc.perform(post("/promotion/coupon/receive/3")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("领取优惠券失败 - 已领取")
    void testReceiveCouponAlreadyReceived() throws Exception {
        mockMvc.perform(post("/promotion/coupon/receive/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("获取我的优惠券列表")
    void testGetMyCoupons() throws Exception {
        mockMvc.perform(get("/promotion/coupon/my")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取我的优惠券列表 - 按状态筛选")
    void testGetMyCouponsByStatus() throws Exception {
        mockMvc.perform(get("/promotion/coupon/my")
                        .header("X-User-Id", "1")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
