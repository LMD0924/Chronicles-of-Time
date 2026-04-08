package org.example.generalservice.client;

import org.example.commondb.utils.RestBean;
import org.example.generalservice.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
 * @Author:总会落叶
 * @Date:2026/4/7
 * @Description:
 */
@FeignClient(name = "auth-center", url = "${auth.center.url:http://localhost:8080}")
public interface UserServiceClient {

    @GetMapping("/api/user/public/{id}")
    RestBean<UserVO> getAuthorInfo(@PathVariable("id") Long id);
}
