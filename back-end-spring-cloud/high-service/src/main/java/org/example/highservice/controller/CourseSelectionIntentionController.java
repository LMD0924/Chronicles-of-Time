package org.example.highservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.commondb.utils.RestBean;
import org.example.highservice.entity.CourseSelectionIntention;
import org.example.highservice.service.CourseSelectionIntentionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/intention")
@RequiredArgsConstructor
public class CourseSelectionIntentionController {

    private final CourseSelectionIntentionService intentionService;

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
    public RestBean<CourseSelectionIntention> save(@RequestBody CourseSelectionIntention intention,
                                                    HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return RestBean.fail("用户未登录");
        intention.setStudentId(userId);
        CourseSelectionIntention saved = intentionService.saveOrUpdateByStudent(intention);
        return RestBean.success("保存成功", saved);
    }

    @GetMapping("/list")
    public RestBean<List<CourseSelectionIntention>> list(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return RestBean.fail("用户未登录");
        return RestBean.success(intentionService.listByStudentId(userId));
    }

    @GetMapping("/detail")
    public RestBean<CourseSelectionIntention> detail(@RequestParam Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return RestBean.fail("用户未登录");
        CourseSelectionIntention item = intentionService.getById(id);
        if (item == null || !userId.equals(item.getStudentId())) return RestBean.fail("记录不存在");
        return RestBean.success(item);
    }

    @PostMapping("/status")
    public RestBean<Integer> updateStatus(@RequestParam Long id,
                                          @RequestParam Integer status,
                                          HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return RestBean.fail("用户未登录");
        CourseSelectionIntention item = intentionService.getById(id);
        if (item == null || !userId.equals(item.getStudentId())) return RestBean.fail("无权操作");
        item.setStatus(status);
        return RestBean.success(intentionService.updateById(item) ? 1 : 0);
    }

    @PostMapping("/delete")
    public RestBean<Integer> delete(@RequestParam Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return RestBean.fail("用户未登录");
        CourseSelectionIntention item = intentionService.getById(id);
        if (item == null || !userId.equals(item.getStudentId())) return RestBean.fail("无权删除");
        return RestBean.success(intentionService.removeById(id) ? 1 : 0);
    }
}
