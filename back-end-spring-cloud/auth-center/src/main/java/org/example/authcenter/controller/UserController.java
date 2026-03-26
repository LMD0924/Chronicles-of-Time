package org.example.authcenter.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.authcenter.entity.User;
import org.example.authcenter.service.UserService;
import org.example.authcenter.vo.UserVO;
import org.example.commondb.utils.RestBean;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * 上传头像
     */
    @PostMapping("/uploadAvatar")
    public RestBean<String> uploadAvatar(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String avatarUrl = params.get("avatarUrl");
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return RestBean.fail("头像URL不能为空");
        }

        // ✅ 先获取 userIdStr，判断是否为 null
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return RestBean.fail(401, "未登录，无法获取用户信息");
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return RestBean.fail(401, "用户ID格式错误");
        }

        boolean success = userService.uploadAvatar(userId, avatarUrl);
        if (success) {
            User user = userService.getUserById(userId);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return RestBean.success("头像上传成功", userVO.getAvatar());
        }
        return RestBean.fail("头像上传失败");
    }
}
