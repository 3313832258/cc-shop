package com.ccshop.trade;

import com.ccshop.common.core.Result;
import com.ccshop.trade.feign.ProductClient;
import com.ccshop.trade.feign.ProductClient.SkuVO;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        ValueOperations valueOps = Mockito.mock(ValueOperations.class);
        HashOperations hashOps = Mockito.mock(HashOperations.class);
        when(template.opsForValue()).thenReturn(valueOps);
        when(template.opsForHash()).thenReturn(hashOps);
        return template;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return Mockito.mock(RabbitTemplate.class);
    }

    @Bean
    public ProductClient productClient() {
        ProductClient client = Mockito.mock(ProductClient.class);
        SkuVO sku = new SkuVO();
        sku.setId(1L);
        sku.setProductId(1L);
        sku.setProductName("iPhone 15");
        sku.setImage("https://example.com/iphone15.jpg");
        Map<String, Object> specs = new HashMap<>();
        specs.put("颜色", "黑色");
        specs.put("存储", "128GB");
        sku.setSpecs(specs);
        sku.setPrice(new BigDecimal("5999.00"));
        sku.setOriginalPrice(new BigDecimal("6999.00"));
        sku.setStock(100);
        when(client.getSku(anyLong())).thenReturn(Result.success(sku));
        return client;
    }
}
