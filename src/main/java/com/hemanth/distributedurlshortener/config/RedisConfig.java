package com.hemanth.distributedurlshortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // Serialize keys as Strings
        template.setKeySerializer(new StringRedisSerializer());

        // Serialize values as Strings
        template.setValueSerializer(new StringRedisSerializer());

        // Serialize hash keys
        template.setHashKeySerializer(new StringRedisSerializer());

        // Serialize hash values
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();

        return template;
    }
}