/**
 * 文件说明：拾光记微服务后端业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.workplaceservice.service;

import org.example.commoncore.auth.AuthUser;
import org.example.workplaceservice.entity.CareerGoal;
import org.example.workplaceservice.entity.CareerProfile;
import org.example.workplaceservice.entity.CareerTask;
import org.example.workplaceservice.entity.InterviewPrep;
import org.example.workplaceservice.entity.WorkReview;

import java.util.List;
import java.util.Map;
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

public interface WorkplaceService {
    CareerProfile saveProfile(CareerProfile profile, AuthUser user);
    CareerProfile getProfile(Long userId, AuthUser user);
    List<CareerGoal> listGoals(String status, AuthUser user);
    CareerGoal saveGoal(CareerGoal goal, AuthUser user);
    boolean deleteGoal(Long id, AuthUser user);
    List<CareerTask> listTasks(Long goalId, String status, AuthUser user);
    CareerTask saveTask(CareerTask task, AuthUser user);
    boolean deleteTask(Long id, AuthUser user);
    List<InterviewPrep> listInterviews(String status, AuthUser user);
    InterviewPrep saveInterview(InterviewPrep interview, AuthUser user);
    List<WorkReview> listReviews(String reviewType, AuthUser user);
    WorkReview saveReview(WorkReview review, AuthUser user);
    Map<String, Object> dashboard(AuthUser user);
}
