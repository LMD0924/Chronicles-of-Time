package org.example.authcenter.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authcenter.entity.User;
import org.example.authcenter.service.UserService;
import org.example.authcenter.vo.UserVO;
import org.example.commoncore.utils.MyBeanUtils;
import org.example.commondb.utils.RestBean;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/3/24
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据id获取当前用户信息
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
     * 根据id获取用户信息
     */
    @GetMapping("/public/{id}")
    public RestBean<UserVO> getAuthorInfo(@PathVariable("id") Long id){
        User userInfo = userService.getUserById(id);
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

    /**
     * 修改用户信息
     * */
    @PutMapping("/updateUserInfo")
    public RestBean<UserVO> updateUserInfo(@Valid @RequestBody UserVO userVO,
                                           HttpServletRequest request) {
        log.info("用户信息：{}", userVO);
        // 1. 获取当前用户ID（如果没有传）
        if (userVO.getId() == null) {
            String userIdStr = request.getHeader("X-User-Id");
            if (userIdStr == null) {
                return RestBean.fail("用户不存在");
            }
            Long userId = Long.parseLong(userIdStr);
            userVO.setId(userId);
        }

        // 2. 检查用户是否存在
        User existingUser = userService.getUserById(userVO.getId());
        if (existingUser == null) {
            return RestBean.fail("用户不存在");
        }

        // 3. 使用 BeanUtils 复制需要更新的字段
        MyBeanUtils.copyNonNullProperties(userVO, existingUser,
                "id", "password", "createTime", "updateTime");

        // 4. 更新用户信息
        boolean success = userService.updateUserInfo(existingUser);

        if (success) {
            User latestUser = userService.getUserById(userVO.getId());
            UserVO latestUserVO = new UserVO();
            BeanUtils.copyProperties(latestUser, latestUserVO);
            return RestBean.success("更新成功", latestUserVO);
        } else {
            return RestBean.fail("更新失败");
        }
    }

}
