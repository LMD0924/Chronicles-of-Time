/**
 * 文件说明：拾光记微服务后端网关应用启动源码，负责应用启动相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "org.example.gateway",
        "org.example.commoncore"
})
/**
 * 类说明：当前类是应用启动模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
