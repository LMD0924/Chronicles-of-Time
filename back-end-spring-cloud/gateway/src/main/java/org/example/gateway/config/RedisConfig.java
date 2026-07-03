/**
 * 文件说明：拾光记微服务后端网关系统配置源码，负责系统配置相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
//package org.example.gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
///*
// * @Author:总会落叶
// * @Date:2026/3/25
// * @Description: gateway模块Redis配置（响应式）
// */
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
//            ReactiveRedisConnectionFactory connectionFactory) {
//
//        StringRedisSerializer serializer = new StringRedisSerializer();
//
//        RedisSerializationContext<String, String> serializationContext =
//                RedisSerializationContext
//                        .<String, String>newSerializationContext(serializer)
//                        .key(serializer)
//                        .value(serializer)
//                        .hashKey(serializer)
//                        .hashValue(serializer)
//                        .build();
//
//        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
//    }
//}