package org.example.commoncore.config;

/*
 * @Author:总会落叶
 * @Date:2026/3/30
 * @Description:
 */
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // 包装类 Long
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            // 基本类型 long
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
