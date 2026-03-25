package org.example.commondb.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
@Configuration
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 其他插件...
        return interceptor;
    }

    // 枚举自动转换配置
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.setDefaultEnumTypeHandler(MybatisEnumTypeHandler.class);
    }
}