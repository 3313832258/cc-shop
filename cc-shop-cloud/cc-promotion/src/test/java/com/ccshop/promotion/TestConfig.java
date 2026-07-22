package com.ccshop.promotion;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        ValueOperations valueOps = Mockito.mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(valueOps);
        return template;
    }
}
