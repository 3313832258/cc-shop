package com.ccshop.user.controller;

import com.ccshop.user.dto.AddressRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("地址控制器测试")
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("获取地址列表")
    void testGetAddressList() throws Exception {
        mockMvc.perform(get("/user/address")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取地址详情")
    void testGetAddressById() throws Exception {
        mockMvc.perform(get("/user/address/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("添加地址成功")
    void testAddAddressSuccess() throws Exception {
        AddressRequest request = new AddressRequest();
        request.setReceiverName("王五");
        request.setProvince("广东省");
        request.setCity("深圳市");
        request.setDistrict("南山区");
        request.setDetail("科技园路1号");
        request.setPhone("13700137000");
        request.setIsDefault(0);

        mockMvc.perform(post("/user/address")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    @DisplayName("添加地址失败 - 缺少必填字段")
    void testAddAddressFailMissingFields() throws Exception {
        AddressRequest request = new AddressRequest();
        request.setReceiverName("王五");
        // 缺少省份、城市、区县、详细地址、电话

        mockMvc.perform(post("/user/address")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("更新地址成功")
    void testUpdateAddressSuccess() throws Exception {
        AddressRequest request = new AddressRequest();
        request.setReceiverName("张三（已更新）");
        request.setProvince("北京市");
        request.setCity("北京市");
        request.setDistrict("海淀区");
        request.setDetail("中关村大街1号");
        request.setPhone("13800138000");
        request.setIsDefault(1);

        mockMvc.perform(put("/user/address/1")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除地址成功")
    void testDeleteAddressSuccess() throws Exception {
        mockMvc.perform(delete("/user/address/2")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("设置默认地址成功")
    void testSetDefaultAddressSuccess() throws Exception {
        mockMvc.perform(put("/user/address/1/default")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
