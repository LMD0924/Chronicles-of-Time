/**
 * 文件说明：拾光记微服务后端文件上传服务系统配置源码，负责系统配置相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.fileupload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 类说明：当前类是系统配置模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
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