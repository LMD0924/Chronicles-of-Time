package org.example.fileupload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path:./uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ 配置静态资源访问：/files/** 映射到 uploads 目录
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath);

        // 打印日志，确认映射路径
        System.out.println("静态资源映射: /files/** -> file:" + uploadPath);
    }
}