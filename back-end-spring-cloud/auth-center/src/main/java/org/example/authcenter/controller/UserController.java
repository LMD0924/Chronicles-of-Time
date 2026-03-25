package org.example.authcenter.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.authcenter.entity.User;
import org.example.authcenter.service.UserService;
import org.example.authcenter.vo.UserVO;
import org.example.commondb.utils.RestBean;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @Author:总会落叶
 * @Date:2026/3/24
 * @Description:
 */
@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据id获取用户信息
     */
    @GetMapping("/getUserById")
    public RestBean<UserVO> getUserById(HttpServletRequest request){
        String userIdStr = request.getHeader("X-User-Id");
        if(userIdStr == null){
            return RestBean.fail("用户不存在");
        }
        Long userId = Long.parseLong(userIdStr);
        User userInfo = userService.getUserById(userId);
        if(userInfo == null) {
            return RestBean.fail("用户不存在");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userInfo, userVO);
        return RestBean.success(userVO);
    }

}
