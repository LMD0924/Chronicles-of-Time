/**
 * 文件说明：拾光记微服务后端业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.workplaceservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.commoncore.auth.AuthUser;
import org.example.commoncore.auth.RoleCodes;
import org.example.workplaceservice.entity.CareerGoal;
import org.example.workplaceservice.entity.CareerProfile;
import org.example.workplaceservice.entity.CareerTask;
import org.example.workplaceservice.entity.InterviewPrep;
import org.example.workplaceservice.entity.WorkReview;
import org.example.workplaceservice.mapper.CareerGoalMapper;
import org.example.workplaceservice.mapper.CareerProfileMapper;
import org.example.workplaceservice.mapper.CareerTaskMapper;
import org.example.workplaceservice.mapper.InterviewPrepMapper;
import org.example.workplaceservice.mapper.WorkReviewMapper;
import org.example.workplaceservice.service.WorkplaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Service
@RequiredArgsConstructor
public class WorkplaceServiceImpl implements WorkplaceService {

    private final CareerProfileMapper profileMapper;
    private final CareerGoalMapper goalMapper;
    private final CareerTaskMapper taskMapper;
    private final InterviewPrepMapper interviewMapper;
    private final WorkReviewMapper reviewMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CareerProfile saveProfile(CareerProfile profile, AuthUser user) {
        requireLogin(user);
        Long ownerId = profile.getUserId() == null ? user.getUserId() : profile.getUserId();
        requireManage(ownerId, user);
        profile.setUserId(ownerId);
        touch(profile);
        if (profile.getId() == null) {
            profileMapper.insert(profile);
        } else {
            requireManage(existingProfile(profile.getId()).getUserId(), user);
            profileMapper.updateById(profile);
        }
        return profile;
    }

    @Override
    public CareerProfile getProfile(Long userId, AuthUser user) {
        requireLogin(user);
        Long ownerId = userId == null ? user.getUserId() : userId;
        requireManage(ownerId, user);
        return profileMapper.selectOne(new LambdaQueryWrapper<CareerProfile>().eq(CareerProfile::getUserId, ownerId).last("LIMIT 1"));
    }

    @Override
    public List<CareerGoal> listGoals(String status, AuthUser user) {
        requireLogin(user);
        LambdaQueryWrapper<CareerGoal> wrapper = ownerScoped(new LambdaQueryWrapper<>(), CareerGoal::getUserId, user);
        if (StringUtils.hasText(status)) {
            wrapper.eq(CareerGoal::getStatus, status);
        }
        return goalMapper.selectList(wrapper.orderByDesc(CareerGoal::getTargetDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CareerGoal saveGoal(CareerGoal goal, AuthUser user) {
        requireLogin(user);
        Long ownerId = goal.getUserId() == null ? user.getUserId() : goal.getUserId();
        requireManage(ownerId, user);
        if (goal.getProgress() == null) {
            goal.setProgress(0);
        }
        if (!StringUtils.hasText(goal.getStatus())) {
            goal.setStatus("ACTIVE");
        }
        goal.setUserId(ownerId);
        touch(goal);
        if (goal.getId() == null) {
            goalMapper.insert(goal);
        } else {
            requireManage(existingGoal(goal.getId()).getUserId(), user);
            goalMapper.updateById(goal);
        }
        return goal;
    }

    @Override
    public boolean deleteGoal(Long id, AuthUser user) {
        CareerGoal goal = existingGoal(id);
        requireManage(goal.getUserId(), user);
        taskMapper.delete(new LambdaQueryWrapper<CareerTask>().eq(CareerTask::getGoalId, id).eq(CareerTask::getUserId, goal.getUserId()));
        return goalMapper.deleteById(id) > 0;
    }

    @Override
    public List<CareerTask> listTasks(Long goalId, String status, AuthUser user) {
        requireLogin(user);
        LambdaQueryWrapper<CareerTask> wrapper = ownerScoped(new LambdaQueryWrapper<>(), CareerTask::getUserId, user);
        if (goalId != null) {
            wrapper.eq(CareerTask::getGoalId, goalId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(CareerTask::getStatus, status);
        }
        return taskMapper.selectList(wrapper.orderByAsc(CareerTask::getDueDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CareerTask saveTask(CareerTask task, AuthUser user) {
        requireLogin(user);
        Long ownerId = task.getUserId() == null ? user.getUserId() : task.getUserId();
        requireManage(ownerId, user);
        task.setUserId(ownerId);
        if (!StringUtils.hasText(task.getStatus())) {
            task.setStatus("TODO");
        }
        touch(task);
        if (task.getId() == null) {
            taskMapper.insert(task);
        } else {
            requireManage(existingTask(task.getId()).getUserId(), user);
            taskMapper.updateById(task);
        }
        refreshGoalProgress(task.getGoalId(), ownerId);
        return task;
    }

    @Override
    public boolean deleteTask(Long id, AuthUser user) {
        CareerTask task = existingTask(id);
        requireManage(task.getUserId(), user);
        boolean deleted = taskMapper.deleteById(id) > 0;
        refreshGoalProgress(task.getGoalId(), task.getUserId());
        return deleted;
    }

    @Override
    public List<InterviewPrep> listInterviews(String status, AuthUser user) {
        requireLogin(user);
        LambdaQueryWrapper<InterviewPrep> wrapper = ownerScoped(new LambdaQueryWrapper<>(), InterviewPrep::getUserId, user);
        if (StringUtils.hasText(status)) {
            wrapper.eq(InterviewPrep::getStatus, status);
        }
        return interviewMapper.selectList(wrapper.orderByAsc(InterviewPrep::getInterviewDate));
    }

    @Override
    public InterviewPrep saveInterview(InterviewPrep interview, AuthUser user) {
        requireLogin(user);
        Long ownerId = interview.getUserId() == null ? user.getUserId() : interview.getUserId();
        requireManage(ownerId, user);
        interview.setUserId(ownerId);
        touch(interview);
        if (interview.getId() == null) {
            interviewMapper.insert(interview);
        } else {
            InterviewPrep existing = interviewMapper.selectById(interview.getId());
            requireManage(existing.getUserId(), user);
            interviewMapper.updateById(interview);
        }
        return interview;
    }

    @Override
    public List<WorkReview> listReviews(String reviewType, AuthUser user) {
        requireLogin(user);
        LambdaQueryWrapper<WorkReview> wrapper = ownerScoped(new LambdaQueryWrapper<>(), WorkReview::getUserId, user);
        if (StringUtils.hasText(reviewType)) {
            wrapper.eq(WorkReview::getReviewType, reviewType);
        }
        return reviewMapper.selectList(wrapper.orderByDesc(WorkReview::getReviewDate));
    }

    @Override
    public WorkReview saveReview(WorkReview review, AuthUser user) {
        requireLogin(user);
        Long ownerId = review.getUserId() == null ? user.getUserId() : review.getUserId();
        requireManage(ownerId, user);
        review.setUserId(ownerId);
        if (review.getReviewDate() == null) {
            review.setReviewDate(LocalDate.now());
        }
        touch(review);
        if (review.getId() == null) {
            reviewMapper.insert(review);
        } else {
            WorkReview existing = reviewMapper.selectById(review.getId());
            requireManage(existing.getUserId(), user);
            reviewMapper.updateById(review);
        }
        return review;
    }

    @Override
    public Map<String, Object> dashboard(AuthUser user) {
        requireLogin(user);
        Map<String, Object> result = new LinkedHashMap<>();
        List<CareerGoal> goals = listGoals(null, user);
        List<CareerTask> tasks = listTasks(null, null, user);
        List<InterviewPrep> interviews = listInterviews(null, user);
        List<WorkReview> reviews = listReviews(null, user);
        result.put("goalCount", goals.size());
        result.put("activeGoalCount", goals.stream().filter(g -> Objects.equals("ACTIVE", g.getStatus())).count());
        result.put("taskCount", tasks.size());
        result.put("todoTaskCount", tasks.stream().filter(t -> !Objects.equals("DONE", t.getStatus())).count());
        result.put("overdueTaskCount", tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) && !Objects.equals("DONE", t.getStatus())).count());
        result.put("interviewCount", interviews.size());
        result.put("upcomingInterviewCount", interviews.stream().filter(i -> i.getInterviewDate() != null && !i.getInterviewDate().isBefore(LocalDate.now())).count());
        result.put("reviewCount", reviews.size());
        result.put("avgDeliveryScore", reviews.stream().filter(r -> r.getDeliveryScore() != null).mapToInt(WorkReview::getDeliveryScore).average().orElse(0));
        result.put("suggestions", buildSuggestions(goals, tasks, interviews, reviews));
        return result;
    }

    private List<String> buildSuggestions(List<CareerGoal> goals, List<CareerTask> tasks, List<InterviewPrep> interviews, List<WorkReview> reviews) {
        java.util.ArrayList<String> suggestions = new java.util.ArrayList<>();
        if (goals.isEmpty()) {
            suggestions.add("先补一个季度职业目标，后续任务和复盘才有锚点。");
        }
        long overdue = tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) && !Objects.equals("DONE", t.getStatus())).count();
        if (overdue > 0) {
            suggestions.add("有 " + overdue + " 个职场任务已逾期，建议今天先处理高优先级项。");
        }
        boolean hasInterview = interviews.stream().anyMatch(i -> i.getInterviewDate() != null && !i.getInterviewDate().isBefore(LocalDate.now()));
        if (hasInterview) {
            suggestions.add("近期有面试安排，建议补充 STAR 案例、岗位问题和复盘反馈。");
        }
        if (reviews.isEmpty()) {
            suggestions.add("还没有工作复盘记录，建议每周记录交付、沟通和能量变化。");
        }
        return suggestions;
    }

    private <T> LambdaQueryWrapper<T> ownerScoped(LambdaQueryWrapper<T> wrapper, com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, Long> userColumn, AuthUser user) {
        if (!user.hasAnyRole(RoleCodes.SUPER_ADMIN, RoleCodes.ADMIN, RoleCodes.TEACHER)) {
            wrapper.eq(userColumn, user.getUserId());
        }
        return wrapper;
    }

    private void requireLogin(AuthUser user) {
        if (user == null || !user.isLogin()) {
            throw new IllegalArgumentException("用户未登录");
        }
    }

    private void requireManage(Long ownerId, AuthUser user) {
        requireLogin(user);
        if (!user.canManageUserResource(ownerId)) {
            throw new SecurityException("无权限访问该职场数据");
        }
    }

    private CareerProfile existingProfile(Long id) {
        CareerProfile profile = profileMapper.selectById(id);
        if (profile == null) {
            throw new IllegalArgumentException("职业档案不存在");
        }
        return profile;
    }

    private CareerGoal existingGoal(Long id) {
        CareerGoal goal = goalMapper.selectById(id);
        if (goal == null) {
            throw new IllegalArgumentException("职业目标不存在");
        }
        return goal;
    }

    private CareerTask existingTask(Long id) {
        CareerTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new IllegalArgumentException("职场任务不存在");
        }
        return task;
    }

    private void refreshGoalProgress(Long goalId, Long userId) {
        if (goalId == null) {
            return;
        }
        List<CareerTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<CareerTask>().eq(CareerTask::getGoalId, goalId).eq(CareerTask::getUserId, userId));
        if (tasks.isEmpty()) {
            return;
        }
        long done = tasks.stream().filter(t -> Objects.equals("DONE", t.getStatus())).count();
        CareerGoal goal = goalMapper.selectById(goalId);
        if (goal != null) {
            goal.setProgress((int) Math.round(done * 100.0 / tasks.size()));
            goal.setUpdatedAt(LocalDateTime.now());
            goalMapper.updateById(goal);
        }
    }

    private void touch(Object target) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Object id = target.getClass().getMethod("getId").invoke(target);
            if (id == null) {
                target.getClass().getMethod("setCreatedAt", LocalDateTime.class).invoke(target, now);
            }
            target.getClass().getMethod("setUpdatedAt", LocalDateTime.class).invoke(target, now);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
