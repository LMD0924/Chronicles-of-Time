package org.example.generalservice.controller.activity;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.dto.activity.HeartbeatDTO;
import org.example.generalservice.dto.activity.MedalRuleDTO;
import org.example.generalservice.entity.activity.MedalRule;
import org.example.generalservice.service.activity.ActivityService;
import org.example.generalservice.vo.activity.ActivitySummaryVO;
import org.example.generalservice.vo.activity.AdminActivityUserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping("/checkin")
    public RestBean<ActivitySummaryVO> checkIn(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        return RestBean.success("打卡成功", activityService.checkIn(userId));
    }

    @PostMapping("/heartbeat")
    public RestBean<ActivitySummaryVO> heartbeat(@RequestBody(required = false) HeartbeatDTO dto,
                                                 HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        return RestBean.success("在线时长已更新", activityService.heartbeat(userId, dto));
    }

    @GetMapping("/summary")
    public RestBean<ActivitySummaryVO> summary(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        return RestBean.success(activityService.summary(userId));
    }

    @GetMapping("/admin/users")
    public RestBean<List<AdminActivityUserVO>> adminUsers(@RequestParam(required = false) String keyword,
                                                         @RequestParam(defaultValue = "5") Integer onlineMinutes) {
        return RestBean.success(activityService.adminUserStats(keyword, onlineMinutes));
    }

    @GetMapping("/admin/medal-rules")
    public RestBean<List<MedalRule>> medalRules() {
        return RestBean.success(activityService.medalRules());
    }

    @PostMapping("/admin/medal-rules")
    public RestBean<MedalRule> saveMedalRule(@RequestBody MedalRuleDTO dto) {
        return RestBean.success("保存成功", activityService.saveMedalRule(dto));
    }

    @PostMapping("/admin/medal-rules/{id}/status")
    public RestBean<Boolean> updateMedalRuleStatus(@PathVariable Long id,
                                                   @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body == null ? null : body.get("enabled");
        boolean result = activityService.updateMedalRuleStatus(id, enabled);
        return result ? RestBean.success("更新成功", true) : RestBean.fail("勋章规则不存在");
    }

    private Long currentUserId(HttpServletRequest request) {
        String value = request.getHeader("X-User-Id");
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
