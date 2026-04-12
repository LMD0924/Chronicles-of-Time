package org.example.universityservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@MapperScan("org.example.universityservice.mapper")
public class UniversityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UniversityServiceApplication.class, args);
    }
}
