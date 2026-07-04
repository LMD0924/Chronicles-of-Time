/**
 * 文件说明：拾光记微服务后端业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.advancedservice.service;

import org.example.advancedservice.entity.AdvancementMilestone;
import org.example.advancedservice.entity.AdvancementRoadmap;
import org.example.advancedservice.entity.MentorSession;
import org.example.advancedservice.entity.SkillProgress;
import org.example.commoncore.auth.AuthUser;

import java.util.List;
import java.util.Map;
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

public interface AdvancedService {
    List<AdvancementRoadmap> listRoadmaps(String status, AuthUser user);
    AdvancementRoadmap saveRoadmap(AdvancementRoadmap roadmap, AuthUser user);
    boolean deleteRoadmap(Long id, AuthUser user);
    List<AdvancementMilestone> listMilestones(Long roadmapId, String status, AuthUser user);
    AdvancementMilestone saveMilestone(AdvancementMilestone milestone, AuthUser user);
    List<SkillProgress> listSkills(Long roadmapId, String category, AuthUser user);
    SkillProgress saveSkill(SkillProgress skill, AuthUser user);
    List<MentorSession> listMentorSessions(Long roadmapId, AuthUser user);
    MentorSession saveMentorSession(MentorSession session, AuthUser user);
    Map<String, Object> dashboard(AuthUser user);
}
