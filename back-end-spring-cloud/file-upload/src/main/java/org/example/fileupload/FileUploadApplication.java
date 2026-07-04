/**
 * 文件说明：拾光记微服务后端文件上传服务应用启动源码，负责应用启动相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.fileupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,           // 排除数据源自动配置
        HibernateJpaAutoConfiguration.class          // 排除 JPA 自动配置
})
/**
 * 类说明：当前类是应用启动模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@EnableDiscoveryClient
@ComponentScan(basePackages = {"org.example.fileupload", "org.example.commoncore"})
public class FileUploadApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileUploadApplication.class, args);
    }
}