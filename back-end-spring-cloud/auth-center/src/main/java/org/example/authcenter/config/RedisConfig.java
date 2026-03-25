//package org.example.authcenter.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
///*
// * @Author:总会落叶
// * @Date:2026/3/25
// * @Description: auth模块Redis配置
// */
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, String> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        // 使用 StringRedisSerializer 来序列化 key
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//
//        // 使用 StringRedisSerializer 来序列化 value（存字符串）
//        template.setValueSerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(new StringRedisSerializer());
//
//        template.afterPropertiesSet();
//        return template;
//    }
//}