package org.example.universityservice.controller.campus;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.commondb.utils.RestBean;
import org.example.universityservice.entity.campus.CampusActivity;
import org.example.universityservice.entity.campus.CampusOrganization;
import org.example.universityservice.mapper.campus.CampusActivityMapper;
import org.example.universityservice.mapper.campus.CampusOrganizationMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/university/campus")
@RequiredArgsConstructor
public class CampusOrganizationController {
    private final CampusOrganizationMapper organizationMapper;
    private final CampusActivityMapper activityMapper;

    @GetMapping("/organizations")
    public RestBean<List<CampusOrganization>> organizations(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "用户未登录");
        return RestBean.success(organizationMapper.selectList(new LambdaQueryWrapper<CampusOrganization>()
                .eq(CampusOrganization::getUserId, userId)
                .orderByDesc(CampusOrganization::getStartDate, CampusOrganization::getCreatedAt)));
    }

    @PostMapping("/organizations")
    public RestBean<CampusOrganization> createOrganization(@RequestBody CampusOrganization body, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "用户未登录");
        if (!StringUtils.hasText(body.getOrganizationName())) return RestBean.fail(400, "组织名称不能为空");
        body.setId(null);
        body.setUserId(userId);
        body.setOrganizationType(defaultText(body.getOrganizationType(), "CLUB"));
        body.setStatus(defaultText(body.getStatus(), "ACTIVE"));
        body.setCreatedAt(LocalDateTime.now());
        body.setUpdatedAt(LocalDateTime.now());
        organizationMapper.insert(body);
        return RestBean.success(body);
    }

    @PutMapping("/organizations/{id}")
    public RestBean<CampusOrganization> updateOrganization(@PathVariable Long id, @RequestBody CampusOrganization body,
                                                            HttpServletRequest request) {
        Long userId = currentUserId(request);
        CampusOrganization current = organizationMapper.selectById(id);
        if (userId == null || current == null || !Objects.equals(current.getUserId(), userId)) return RestBean.fail(404, "组织经历不存在");
        body.setId(id);
        body.setUserId(userId);
        body.setCreatedAt(current.getCreatedAt());
        body.setUpdatedAt(LocalDateTime.now());
        organizationMapper.updateById(body);
        return RestBean.success(body);
    }

    @DeleteMapping("/organizations/{id}")
    @Transactional(rollbackFor = Exception.class)
    public RestBean<Boolean> deleteOrganization(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        CampusOrganization current = organizationMapper.selectById(id);
        if (userId == null || current == null || !Objects.equals(current.getUserId(), userId)) return RestBean.fail(404, "组织经历不存在");
        activityMapper.delete(new LambdaQueryWrapper<CampusActivity>().eq(CampusActivity::getUserId, userId).eq(CampusActivity::getOrganizationId, id));
        organizationMapper.deleteById(id);
        return RestBean.success(true);
    }

    @GetMapping("/activities")
    public RestBean<List<CampusActivity>> activities(@RequestParam(required = false) Long organizationId,
                                                      HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "用户未登录");
        LambdaQueryWrapper<CampusActivity> query = new LambdaQueryWrapper<CampusActivity>().eq(CampusActivity::getUserId, userId);
        if (organizationId != null) query.eq(CampusActivity::getOrganizationId, organizationId);
        query.orderByDesc(CampusActivity::getStartAt, CampusActivity::getCreatedAt);
        return RestBean.success(activityMapper.selectList(query));
    }

    @PostMapping("/activities")
    public RestBean<CampusActivity> createActivity(@RequestBody CampusActivity body, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "用户未登录");
        if (!StringUtils.hasText(body.getTitle())) return RestBean.fail(400, "事务名称不能为空");
        assertOrganization(userId, body.getOrganizationId());
        body.setId(null);
        body.setUserId(userId);
        body.setActivityType(defaultText(body.getActivityType(), "ACTIVITY"));
        body.setStatus(defaultText(body.getStatus(), "PLANNED"));
        body.setCreatedAt(LocalDateTime.now());
        body.setUpdatedAt(LocalDateTime.now());
        activityMapper.insert(body);
        return RestBean.success(body);
    }

    @PutMapping("/activities/{id}")
    public RestBean<CampusActivity> updateActivity(@PathVariable Long id, @RequestBody CampusActivity body,
                                                    HttpServletRequest request) {
        Long userId = currentUserId(request);
        CampusActivity current = activityMapper.selectById(id);
        if (userId == null || current == null || !Objects.equals(current.getUserId(), userId)) return RestBean.fail(404, "校园事务不存在");
        assertOrganization(userId, body.getOrganizationId());
        body.setId(id);
        body.setUserId(userId);
        body.setCreatedAt(current.getCreatedAt());
        body.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(body);
        return RestBean.success(body);
    }

    @DeleteMapping("/activities/{id}")
    public RestBean<Boolean> deleteActivity(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        CampusActivity current = activityMapper.selectById(id);
        if (userId == null || current == null || !Objects.equals(current.getUserId(), userId)) return RestBean.fail(404, "校园事务不存在");
        activityMapper.deleteById(id);
        return RestBean.success(true);
    }

    @GetMapping("/summary")
    public RestBean<Map<String, Object>> summary(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return RestBean.fail(401, "用户未登录");
        List<CampusOrganization> organizations = organizationMapper.selectList(new LambdaQueryWrapper<CampusOrganization>().eq(CampusOrganization::getUserId, userId));
        List<CampusActivity> activities = activityMapper.selectList(new LambdaQueryWrapper<CampusActivity>().eq(CampusActivity::getUserId, userId));
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("organizationCount", organizations.size());
        summary.put("activeOrganizationCount", organizations.stream().filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus())).count());
        summary.put("activityCount", activities.size());
        summary.put("completedActivityCount", activities.stream().filter(item -> "DONE".equalsIgnoreCase(item.getStatus())).count());
        summary.put("serviceHours", activities.stream().map(CampusActivity::getServiceHours).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        return RestBean.success(summary);
    }

    private void assertOrganization(Long userId, Long organizationId) {
        if (organizationId == null) return;
        CampusOrganization organization = organizationMapper.selectById(organizationId);
        if (organization == null || !Objects.equals(organization.getUserId(), userId)) throw new IllegalArgumentException("组织经历不存在");
    }

    private String defaultText(String value, String fallback) { return StringUtils.hasText(value) ? value : fallback; }
    private Long currentUserId(HttpServletRequest request) {
        try { return Long.valueOf(request.getHeader("X-User-Id")); } catch (Exception ignored) { return null; }
    }
}
