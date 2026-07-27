package com.ccshop.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通义千问 DashScope 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeConfig {

    /** API Key */
    private String apiKey;

    /** 模型名称，默认 qwen-turbo */
    private String model = "qwen-turbo";

    /** API 基地址 */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
}
