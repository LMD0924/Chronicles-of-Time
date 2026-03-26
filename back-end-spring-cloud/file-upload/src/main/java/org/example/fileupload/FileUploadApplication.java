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
@EnableDiscoveryClient
@ComponentScan(basePackages = {"org.example.fileupload", "org.example.commoncore"})
public class FileUploadApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileUploadApplication.class, args);
    }
}