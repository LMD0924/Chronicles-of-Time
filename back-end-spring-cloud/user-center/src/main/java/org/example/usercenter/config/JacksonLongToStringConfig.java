/**
 * 文件说明：拾光记微服务后端用户中心系统配置源码，负责系统配置相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.usercenter.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 类说明：当前类是系统配置模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Configuration
public class JacksonLongToStringConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonLongCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
