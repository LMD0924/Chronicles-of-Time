/**
 * 文件说明：拾光记微服务后端高中服务接口控制器源码，负责接口控制器相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.commondb.utils.RestBean;
import org.example.highservice.entity.CourseGuidance;
import org.example.highservice.service.CourseGuidanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 类说明：当前类是接口控制器模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@RestController
@RequestMapping("api/guidance")
@RequiredArgsConstructor
public class CourseGuidanceController {

    private final CourseGuidanceService guidanceService;

    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) return null;
        try {
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/save")
    public RestBean<CourseGuidance> save(@RequestBody CourseGuidance guidance,
                                         HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return RestBean.fail("用户未登录");
        guidance.setUserId(userId);
        CourseGuidance saved = guidanceService.saveOrUpdateByStudent(guidance);
        return RestBean.success("保存成功", saved);
    }

    @GetMapping("/list")
    public RestBean<List<CourseGuidance>> list(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return RestBean.fail("用户未登录");
        return RestBean.success(guidanceService.listByUserId(userId));
    }

    @GetMapping("/detail")
    public RestBean<CourseGuidance> detail(@RequestParam Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return RestBean.fail("用户未登录");
        CourseGuidance item = guidanceService.getById(id);
        if (item == null || !userId.equals(item.getUserId())) return RestBean.fail("记录不存在");
        return RestBean.success(item);
    }

    @PostMapping("/status")
    public RestBean<Integer> updateStatus(@RequestParam Long id,
                                          @RequestParam Integer status,
                                          HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return RestBean.fail("用户未登录");
        CourseGuidance item = guidanceService.getById(id);
        if (item == null || !userId.equals(item.getUserId())) return RestBean.fail("无权操作");
        item.setStatus(status);
        return RestBean.success(guidanceService.updateById(item) ? 1 : 0);
    }

    @PostMapping("/delete")
    public RestBean<Integer> delete(@RequestParam Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return RestBean.fail("用户未登录");
        CourseGuidance item = guidanceService.getById(id);
        if (item == null || !userId.equals(item.getUserId())) return RestBean.fail("无权删除");
        return RestBean.success(guidanceService.removeById(id) ? 1 : 0);
    }
}
