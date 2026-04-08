package org.example.usercenter.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.commondb.utils.RestBean;
import org.example.usercenter.entity.UserInfo;
import org.example.usercenter.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2026/4/5
 * @Description: 用户信息控制器 - 提供增改查接口 (使用RestBean统一返回)
 */
@RestController
@RequestMapping("api/userInfo")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 从请求头获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return null;
        }
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 新增用户信息
     * POST /api/userInfo/add
     */
    @PostMapping("/add")
    public RestBean<String> add(@RequestBody @Valid UserInfo userInfo, HttpServletRequest request) {
        // 获取当前登录用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return RestBean.fail("用户未登录");
        }

        // 设置用户ID
        userInfo.setUserId(userId);

        boolean success = userInfoService.addUserInfo(userInfo);
        if (success) {
            return RestBean.success("新增成功");
        }
        return RestBean.fail("新增失败");
    }

    /**
     * 修改用户信息
     * PUT /api/userInfo/update
     */
    @PutMapping("/update")
    public RestBean<String> update(@RequestBody @Valid UserInfo userInfo, HttpServletRequest request) {
        // 获取当前登录用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return RestBean.fail("用户未登录");
        }

        if (userInfo.getId() == null) {
            return RestBean.fail("ID不能为空");
        }

        // 校验要修改的记录是否属于当前用户
        UserInfo existingInfo = userInfoService.getUserInfoById(userInfo.getId());
        if (existingInfo == null) {
            return RestBean.fail("记录不存在");
        }
        if (!existingInfo.getUserId().equals(userId)) {
            return RestBean.fail("无权修改此记录");
        }

        boolean success = userInfoService.updateUserInfo(userInfo);
        if (success) {
            return RestBean.success("修改成功");
        }
        return RestBean.fail("修改失败");
    }

    /**
     * 根据ID查询
     * GET /api/userInfo/get/{id}
     */
    @GetMapping("/get/{id}")
    public RestBean<UserInfo> getById(@PathVariable Long id, HttpServletRequest request) {
        // 获取当前登录用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return RestBean.fail("用户未登录");
        }

        UserInfo userInfo = userInfoService.getUserInfoById(id);
        if (userInfo == null) {
            return RestBean.fail("未找到该记录");
        }

        // 校验是否为当前用户的数据
        if (!userInfo.getUserId().equals(userId)) {
            return RestBean.fail("无权查看此记录");
        }

        return RestBean.success(userInfo);
    }

    /**
     * 查询当前用户的信息
     * GET /api/userInfo/getCurrent
     */
    @GetMapping("/getCurrent")
    public RestBean<UserInfo> getCurrent(HttpServletRequest request) {
        // 获取当前登录用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return RestBean.fail("用户未登录");
        }

        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        if (userInfo == null) {
            return RestBean.fail("未找到该用户的信息");
        }
        return RestBean.success(userInfo);
    }

    /**
     * 查询所有（管理员使用，如需权限控制可添加）
     * GET /api/userInfo/list
     */
    @GetMapping("/list")
    public RestBean<List<UserInfo>> list(HttpServletRequest request) {
        // 可选：添加管理员权限校验
        // 获取当前用户ID，判断是否有管理员权限
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return RestBean.fail("用户未登录");
        }

        List<UserInfo> list = userInfoService.getAllUserInfo();
        return RestBean.success(list);
    }

    /**
     * 根据ID删除
     * DELETE /api/userInfo/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public RestBean<String> delete(@PathVariable Long id, HttpServletRequest request) {
        // 获取当前登录用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return RestBean.fail("用户未登录");
        }

        // 校验要删除的记录是否属于当前用户
        UserInfo existingInfo = userInfoService.getUserInfoById(id);
        if (existingInfo == null) {
            return RestBean.fail("记录不存在");
        }
        if (!existingInfo.getUserId().equals(userId)) {
            return RestBean.fail("无权删除此记录");
        }

        boolean success = userInfoService.deleteUserInfoById(id);
        if (success) {
            return RestBean.success("删除成功");
        }
        return RestBean.fail("删除失败");
    }


}