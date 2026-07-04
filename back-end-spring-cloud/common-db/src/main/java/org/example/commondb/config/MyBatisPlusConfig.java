/**
 * 文件说明：拾光记微服务后端公共数据库系统配置源码，负责系统配置相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是系统配置模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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