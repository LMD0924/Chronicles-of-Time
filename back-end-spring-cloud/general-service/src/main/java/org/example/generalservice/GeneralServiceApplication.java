package org.example.generalservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = {
        "org.example.generalservice.mapper",
        "org.example.highservice.mapper",
})
public class GeneralServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneralServiceApplication.class, args);
    }

}
