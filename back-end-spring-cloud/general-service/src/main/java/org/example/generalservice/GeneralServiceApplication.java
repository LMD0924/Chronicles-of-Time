package org.example.generalservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@MapperScan(basePackages = {
        "org.example.generalservice.mapper",
        "org.example.highservice.mapper",
})
@EnableFeignClients(basePackages = {"org.example.generalservice.client"})
public class GeneralServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneralServiceApplication.class, args);
    }

}
