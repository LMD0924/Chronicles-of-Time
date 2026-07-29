/**
 * 文件说明：拾光记微服务后端接口控制器源码，负责接口控制器相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.workplaceservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.commoncore.auth.AuthContext;
import org.example.commoncore.auth.AuthUser;
import org.example.commondb.utils.RestBean;
import org.example.workplaceservice.entity.CareerGoal;
import org.example.workplaceservice.entity.CareerProfile;
import org.example.workplaceservice.entity.CareerTask;
import org.example.workplaceservice.entity.InterviewPrep;
import org.example.workplaceservice.entity.WorkReview;
import org.example.workplaceservice.service.WorkplaceService;
import org.example.workplaceservice.service.InterviewSimulationService;
import org.example.workplaceservice.dto.InterviewTurnRequest;
import org.example.workplaceservice.dto.InterviewTurnResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 类说明：当前类是接口控制器模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@RestController
@RequestMapping("api/workplace")
@RequiredArgsConstructor
public class WorkplaceController {

    private final WorkplaceService workplaceService;
    private final InterviewSimulationService interviewSimulationService;

    @GetMapping("/dashboard")
    public RestBean<Map<String, Object>> dashboard(HttpServletRequest request) {
        return RestBean.success(workplaceService.dashboard(current(request)));
    }

    @GetMapping("/profile")
    public RestBean<CareerProfile> getProfile(@RequestParam(required = false) Long userId, HttpServletRequest request) {
        return RestBean.success(workplaceService.getProfile(userId, current(request)));
    }

    @PostMapping("/profile")
    public RestBean<CareerProfile> saveProfile(@RequestBody CareerProfile profile, HttpServletRequest request) {
        return RestBean.success("保存成功", workplaceService.saveProfile(profile, current(request)));
    }

    @GetMapping("/goals")
    public RestBean<List<CareerGoal>> listGoals(@RequestParam(required = false) String status, HttpServletRequest request) {
        return RestBean.success(workplaceService.listGoals(status, current(request)));
    }

    @PostMapping("/goals")
    public RestBean<CareerGoal> saveGoal(@RequestBody CareerGoal goal, HttpServletRequest request) {
        return RestBean.success("保存成功", workplaceService.saveGoal(goal, current(request)));
    }

    @DeleteMapping("/goals/{id}")
    public RestBean<Boolean> deleteGoal(@PathVariable Long id, HttpServletRequest request) {
        return RestBean.success("删除成功", workplaceService.deleteGoal(id, current(request)));
    }

    @GetMapping("/tasks")
    public RestBean<List<CareerTask>> listTasks(@RequestParam(required = false) Long goalId,
                                                @RequestParam(required = false) String status,
                                                HttpServletRequest request) {
        return RestBean.success(workplaceService.listTasks(goalId, status, current(request)));
    }

    @PostMapping("/tasks")
    public RestBean<CareerTask> saveTask(@RequestBody CareerTask task, HttpServletRequest request) {
        return RestBean.success("保存成功", workplaceService.saveTask(task, current(request)));
    }

    @DeleteMapping("/tasks/{id}")
    public RestBean<Boolean> deleteTask(@PathVariable Long id, HttpServletRequest request) {
        return RestBean.success("删除成功", workplaceService.deleteTask(id, current(request)));
    }

    @GetMapping("/interviews")
    public RestBean<List<InterviewPrep>> listInterviews(@RequestParam(required = false) String status, HttpServletRequest request) {
        return RestBean.success(workplaceService.listInterviews(status, current(request)));
    }

    @PostMapping("/interviews")
    public RestBean<InterviewPrep> saveInterview(@RequestBody InterviewPrep interview, HttpServletRequest request) {
        return RestBean.success("保存成功", workplaceService.saveInterview(interview, current(request)));
    }

    @PostMapping("/ai-interview/turn")
    public RestBean<InterviewTurnResponse> interviewTurn(@RequestBody(required = false) InterviewTurnRequest request,
                                                          HttpServletRequest servletRequest) {
        AuthUser user = current(servletRequest);
        if (user == null || !user.isLogin()) {
            return RestBean.fail(401, "User is not logged in");
        }
        return RestBean.success(interviewSimulationService.turn(request));
    }

    @GetMapping("/reviews")
    public RestBean<List<WorkReview>> listReviews(@RequestParam(required = false) String reviewType, HttpServletRequest request) {
        return RestBean.success(workplaceService.listReviews(reviewType, current(request)));
    }

    @PostMapping("/reviews")
    public RestBean<WorkReview> saveReview(@RequestBody WorkReview review, HttpServletRequest request) {
        return RestBean.success("保存成功", workplaceService.saveReview(review, current(request)));
    }

    private AuthUser current(HttpServletRequest request) {
        return AuthContext.currentUser(request);
    }
}
