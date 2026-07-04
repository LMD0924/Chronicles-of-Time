/**
 * 文件说明：拾光记微服务后端接口控制器源码，负责接口控制器相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.advancedservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.advancedservice.entity.AdvancementMilestone;
import org.example.advancedservice.entity.AdvancementRoadmap;
import org.example.advancedservice.entity.MentorSession;
import org.example.advancedservice.entity.SkillProgress;
import org.example.advancedservice.service.AdvancedService;
import org.example.commoncore.auth.AuthContext;
import org.example.commoncore.auth.AuthUser;
import org.example.commondb.utils.RestBean;
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
@RequestMapping("api/advanced")
@RequiredArgsConstructor
public class AdvancedController {

    private final AdvancedService advancedService;

    @GetMapping("/dashboard")
    public RestBean<Map<String, Object>> dashboard(HttpServletRequest request) {
        return RestBean.success(advancedService.dashboard(current(request)));
    }

    @GetMapping("/roadmaps")
    public RestBean<List<AdvancementRoadmap>> listRoadmaps(@RequestParam(required = false) String status, HttpServletRequest request) {
        return RestBean.success(advancedService.listRoadmaps(status, current(request)));
    }

    @PostMapping("/roadmaps")
    public RestBean<AdvancementRoadmap> saveRoadmap(@RequestBody AdvancementRoadmap roadmap, HttpServletRequest request) {
        return RestBean.success("保存成功", advancedService.saveRoadmap(roadmap, current(request)));
    }

    @DeleteMapping("/roadmaps/{id}")
    public RestBean<Boolean> deleteRoadmap(@PathVariable Long id, HttpServletRequest request) {
        return RestBean.success("删除成功", advancedService.deleteRoadmap(id, current(request)));
    }

    @GetMapping("/milestones")
    public RestBean<List<AdvancementMilestone>> listMilestones(@RequestParam(required = false) Long roadmapId,
                                                               @RequestParam(required = false) String status,
                                                               HttpServletRequest request) {
        return RestBean.success(advancedService.listMilestones(roadmapId, status, current(request)));
    }

    @PostMapping("/milestones")
    public RestBean<AdvancementMilestone> saveMilestone(@RequestBody AdvancementMilestone milestone, HttpServletRequest request) {
        return RestBean.success("保存成功", advancedService.saveMilestone(milestone, current(request)));
    }

    @GetMapping("/skills")
    public RestBean<List<SkillProgress>> listSkills(@RequestParam(required = false) Long roadmapId,
                                                    @RequestParam(required = false) String category,
                                                    HttpServletRequest request) {
        return RestBean.success(advancedService.listSkills(roadmapId, category, current(request)));
    }

    @PostMapping("/skills")
    public RestBean<SkillProgress> saveSkill(@RequestBody SkillProgress skill, HttpServletRequest request) {
        return RestBean.success("保存成功", advancedService.saveSkill(skill, current(request)));
    }

    @GetMapping("/mentor-sessions")
    public RestBean<List<MentorSession>> listMentorSessions(@RequestParam(required = false) Long roadmapId, HttpServletRequest request) {
        return RestBean.success(advancedService.listMentorSessions(roadmapId, current(request)));
    }

    @PostMapping("/mentor-sessions")
    public RestBean<MentorSession> saveMentorSession(@RequestBody MentorSession session, HttpServletRequest request) {
        return RestBean.success("保存成功", advancedService.saveMentorSession(session, current(request)));
    }

    private AuthUser current(HttpServletRequest request) {
        return AuthContext.currentUser(request);
    }
}
