/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.client;

import org.example.commondb.utils.RestBean;
import org.example.generalservice.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2026/4/7
 * @Description:
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@FeignClient(name = "auth-center", url = "${auth.center.url:http://localhost:8080}")
public interface UserServiceClient {

    @GetMapping("/api/user/public/{id}")
    RestBean<UserVO> getAuthorInfo(@PathVariable("id") Long id);

    @GetMapping("/api/user/public/search")
    RestBean<List<UserVO>> searchPublicUsers(@RequestParam("keyword") String keyword,
                                             @RequestParam("limit") Integer limit);
}
