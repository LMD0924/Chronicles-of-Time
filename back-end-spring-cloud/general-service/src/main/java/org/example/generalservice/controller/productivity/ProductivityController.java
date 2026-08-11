package org.example.generalservice.controller.productivity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.commoncore.auth.AuthContext;
import org.example.commoncore.auth.AuthUser;
import org.example.commondb.utils.RestBean;
import org.example.generalservice.entity.GrowthWeeklyReport;
import org.example.generalservice.mapper.GrowthWeeklyReportMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("api/productivity")
@RequiredArgsConstructor
public class ProductivityController {

    private final GrowthWeeklyReportMapper weeklyReportMapper;

    @GetMapping("/weekly-report")
    public RestBean<GrowthWeeklyReport> weeklyReport(@RequestParam LocalDate weekStart,
                                                      HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        GrowthWeeklyReport report = weeklyReportMapper.selectOne(new QueryWrapper<GrowthWeeklyReport>()
                .eq("user_id", userId)
                .eq("week_start", weekStart)
                .last("LIMIT 1"));
        return RestBean.success(report);
    }

    @PutMapping("/weekly-report")
    public RestBean<GrowthWeeklyReport> saveWeeklyReport(@RequestBody GrowthWeeklyReport incoming,
                                                          HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return RestBean.fail(401, "用户未登录");
        }
        if (incoming.getWeekStart() == null) {
            return RestBean.fail("周报开始日期不能为空");
        }
        GrowthWeeklyReport report = weeklyReportMapper.selectOne(new QueryWrapper<GrowthWeeklyReport>()
                .eq("user_id", userId)
                .eq("week_start", incoming.getWeekStart())
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (report == null) {
            report = new GrowthWeeklyReport();
            report.setUserId(userId);
            report.setWeekStart(incoming.getWeekStart());
            report.setCreatedAt(now);
        }
        report.setWeekEnd(incoming.getWeekEnd() == null ? incoming.getWeekStart().plusDays(6) : incoming.getWeekEnd());
        report.setReportJson(incoming.getReportJson());
        report.setReflection(incoming.getReflection());
        report.setNextWeekFocus(incoming.getNextWeekFocus());
        report.setUpdatedAt(now);
        if (report.getId() == null) {
            weeklyReportMapper.insert(report);
        } else {
            weeklyReportMapper.updateById(report);
        }
        return RestBean.success("周报已保存", report);
    }

    private Long currentUserId(HttpServletRequest request) {
        AuthUser user = AuthContext.currentUser(request);
        return user != null && user.isLogin() ? user.getUserId() : null;
    }
}
