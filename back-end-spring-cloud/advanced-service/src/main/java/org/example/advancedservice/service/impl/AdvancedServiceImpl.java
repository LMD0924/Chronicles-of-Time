/**
 * 文件说明：拾光记微服务后端业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.advancedservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.advancedservice.entity.AdvancementMilestone;
import org.example.advancedservice.entity.AdvancementRoadmap;
import org.example.advancedservice.entity.MentorSession;
import org.example.advancedservice.entity.SkillProgress;
import org.example.advancedservice.mapper.AdvancementMilestoneMapper;
import org.example.advancedservice.mapper.AdvancementRoadmapMapper;
import org.example.advancedservice.mapper.MentorSessionMapper;
import org.example.advancedservice.mapper.SkillProgressMapper;
import org.example.advancedservice.service.AdvancedService;
import org.example.commoncore.auth.AuthUser;
import org.example.commoncore.auth.RoleCodes;
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
public class AdvancedServiceImpl implements AdvancedService {

    private final AdvancementRoadmapMapper roadmapMapper;
    private final AdvancementMilestoneMapper milestoneMapper;
    private final SkillProgressMapper skillMapper;
    private final MentorSessionMapper mentorSessionMapper;

    @Override
    public List<AdvancementRoadmap> listRoadmaps(String status, AuthUser user) {
        requireLogin(user);
        LambdaQueryWrapper<AdvancementRoadmap> wrapper = ownerScoped(new LambdaQueryWrapper<>(), AdvancementRoadmap::getUserId, user);
        if (StringUtils.hasText(status)) {
            wrapper.eq(AdvancementRoadmap::getStatus, status);
        }
        return roadmapMapper.selectList(wrapper.orderByDesc(AdvancementRoadmap::getUpdatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdvancementRoadmap saveRoadmap(AdvancementRoadmap roadmap, AuthUser user) {
        requireLogin(user);
        Long ownerId = roadmap.getUserId() == null ? user.getUserId() : roadmap.getUserId();
        requireManage(ownerId, user);
        roadmap.setUserId(ownerId);
        if (roadmap.getProgress() == null) {
            roadmap.setProgress(0);
        }
        if (!StringUtils.hasText(roadmap.getStatus())) {
            roadmap.setStatus("ACTIVE");
        }
        touch(roadmap);
        if (roadmap.getId() == null) {
            roadmapMapper.insert(roadmap);
        } else {
            requireManage(existingRoadmap(roadmap.getId()).getUserId(), user);
            roadmapMapper.updateById(roadmap);
        }
        return roadmap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoadmap(Long id, AuthUser user) {
        AdvancementRoadmap roadmap = existingRoadmap(id);
        requireManage(roadmap.getUserId(), user);
        milestoneMapper.delete(new LambdaQueryWrapper<AdvancementMilestone>().eq(AdvancementMilestone::getRoadmapId, id));
        skillMapper.delete(new LambdaQueryWrapper<SkillProgress>().eq(SkillProgress::getRoadmapId, id));
        mentorSessionMapper.delete(new LambdaQueryWrapper<MentorSession>().eq(MentorSession::getRoadmapId, id));
        return roadmapMapper.deleteById(id) > 0;
    }

    @Override
    public List<AdvancementMilestone> listMilestones(Long roadmapId, String status, AuthUser user) {
        requireLogin(user);
        LambdaQueryWrapper<AdvancementMilestone> wrapper = ownerScoped(new LambdaQueryWrapper<>(), AdvancementMilestone::getUserId, user);
        if (roadmapId != null) {
            wrapper.eq(AdvancementMilestone::getRoadmapId, roadmapId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(AdvancementMilestone::getStatus, status);
        }
        return milestoneMapper.selectList(wrapper.orderByAsc(AdvancementMilestone::getDueDate));
    }

    @Override
    public AdvancementMilestone saveMilestone(AdvancementMilestone milestone, AuthUser user) {
        requireLogin(user);
        Long ownerId = milestone.getUserId() == null ? user.getUserId() : milestone.getUserId();
        requireManage(ownerId, user);
        milestone.setUserId(ownerId);
        if (!StringUtils.hasText(milestone.getStatus())) {
            milestone.setStatus("TODO");
        }
        if (Objects.equals("DONE", milestone.getStatus()) && milestone.getCompletedDate() == null) {
            milestone.setCompletedDate(LocalDate.now());
        }
        if (milestone.getWeight() == null) {
            milestone.setWeight(1);
        }
        touch(milestone);
        if (milestone.getId() == null) {
            milestoneMapper.insert(milestone);
        } else {
            AdvancementMilestone existing = milestoneMapper.selectById(milestone.getId());
            requireManage(existing.getUserId(), user);
            milestoneMapper.updateById(milestone);
        }
        refreshRoadmapProgress(milestone.getRoadmapId(), ownerId);
        return milestone;
    }

    @Override
    public List<SkillProgress> listSkills(Long roadmapId, String category, AuthUser user) {
        requireLogin(user);
        LambdaQueryWrapper<SkillProgress> wrapper = ownerScoped(new LambdaQueryWrapper<>(), SkillProgress::getUserId, user);
        if (roadmapId != null) {
            wrapper.eq(SkillProgress::getRoadmapId, roadmapId);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(SkillProgress::getSkillCategory, category);
        }
        return skillMapper.selectList(wrapper.orderByAsc(SkillProgress::getSkillCategory).orderByDesc(SkillProgress::getUpdatedAt));
    }

    @Override
    public SkillProgress saveSkill(SkillProgress skill, AuthUser user) {
        requireLogin(user);
        Long ownerId = skill.getUserId() == null ? user.getUserId() : skill.getUserId();
        requireManage(ownerId, user);
        skill.setUserId(ownerId);
        if (skill.getProgress() == null) {
            skill.setProgress(0);
        }
        touch(skill);
        if (skill.getId() == null) {
            skillMapper.insert(skill);
        } else {
            SkillProgress existing = skillMapper.selectById(skill.getId());
            requireManage(existing.getUserId(), user);
            skillMapper.updateById(skill);
        }
        refreshRoadmapProgress(skill.getRoadmapId(), ownerId);
        return skill;
    }

    @Override
    public List<MentorSession> listMentorSessions(Long roadmapId, AuthUser user) {
        requireLogin(user);
        LambdaQueryWrapper<MentorSession> wrapper = ownerScoped(new LambdaQueryWrapper<>(), MentorSession::getUserId, user);
        if (roadmapId != null) {
            wrapper.eq(MentorSession::getRoadmapId, roadmapId);
        }
        return mentorSessionMapper.selectList(wrapper.orderByDesc(MentorSession::getSessionDate));
    }

    @Override
    public MentorSession saveMentorSession(MentorSession session, AuthUser user) {
        requireLogin(user);
        Long ownerId = session.getUserId() == null ? user.getUserId() : session.getUserId();
        requireManage(ownerId, user);
        session.setUserId(ownerId);
        if (session.getSessionDate() == null) {
            session.setSessionDate(LocalDate.now());
        }
        touch(session);
        if (session.getId() == null) {
            mentorSessionMapper.insert(session);
        } else {
            MentorSession existing = mentorSessionMapper.selectById(session.getId());
            requireManage(existing.getUserId(), user);
            mentorSessionMapper.updateById(session);
        }
        return session;
    }

    @Override
    public Map<String, Object> dashboard(AuthUser user) {
        requireLogin(user);
        List<AdvancementRoadmap> roadmaps = listRoadmaps(null, user);
        List<AdvancementMilestone> milestones = listMilestones(null, null, user);
        List<SkillProgress> skills = listSkills(null, null, user);
        List<MentorSession> sessions = listMentorSessions(null, user);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("roadmapCount", roadmaps.size());
        result.put("activeRoadmapCount", roadmaps.stream().filter(r -> Objects.equals("ACTIVE", r.getStatus())).count());
        result.put("milestoneCount", milestones.size());
        result.put("doneMilestoneCount", milestones.stream().filter(m -> Objects.equals("DONE", m.getStatus())).count());
        result.put("overdueMilestoneCount", milestones.stream().filter(m -> m.getDueDate() != null && m.getDueDate().isBefore(LocalDate.now()) && !Objects.equals("DONE", m.getStatus())).count());
        result.put("skillCount", skills.size());
        result.put("avgSkillProgress", skills.stream().filter(s -> s.getProgress() != null).mapToInt(SkillProgress::getProgress).average().orElse(0));
        result.put("mentorSessionCount", sessions.size());
        result.put("suggestions", buildSuggestions(roadmaps, milestones, skills, sessions));
        return result;
    }

    private List<String> buildSuggestions(List<AdvancementRoadmap> roadmaps, List<AdvancementMilestone> milestones, List<SkillProgress> skills, List<MentorSession> sessions) {
        java.util.ArrayList<String> suggestions = new java.util.ArrayList<>();
        if (roadmaps.isEmpty()) {
            suggestions.add("先建立一条进阶路线，例如从当前岗位到目标岗位的 90 天路径。");
        }
        long overdue = milestones.stream().filter(m -> m.getDueDate() != null && m.getDueDate().isBefore(LocalDate.now()) && !Objects.equals("DONE", m.getStatus())).count();
        if (overdue > 0) {
            suggestions.add("有 " + overdue + " 个进阶里程碑已逾期，建议重新拆分交付物和截止日期。");
        }
        boolean noPractice = skills.stream().noneMatch(s -> s.getLastPracticedAt() != null && s.getLastPracticedAt().isAfter(LocalDate.now().minusDays(14)));
        if (!skills.isEmpty() && noPractice) {
            suggestions.add("核心技能近两周缺少练习记录，建议安排一次可验证的作品或输出。");
        }
        if (sessions.isEmpty()) {
            suggestions.add("还没有导师/复盘会话记录，可以邀请老师、同事或前辈给一次路线反馈。");
        }
        return suggestions;
    }

    private void refreshRoadmapProgress(Long roadmapId, Long userId) {
        if (roadmapId == null) {
            return;
        }
        List<AdvancementMilestone> milestones = milestoneMapper.selectList(new LambdaQueryWrapper<AdvancementMilestone>().eq(AdvancementMilestone::getRoadmapId, roadmapId).eq(AdvancementMilestone::getUserId, userId));
        List<SkillProgress> skills = skillMapper.selectList(new LambdaQueryWrapper<SkillProgress>().eq(SkillProgress::getRoadmapId, roadmapId).eq(SkillProgress::getUserId, userId));
        int milestoneProgress = 0;
        if (!milestones.isEmpty()) {
            int totalWeight = milestones.stream().map(m -> m.getWeight() == null ? 1 : m.getWeight()).mapToInt(Integer::intValue).sum();
            int doneWeight = milestones.stream().filter(m -> Objects.equals("DONE", m.getStatus())).map(m -> m.getWeight() == null ? 1 : m.getWeight()).mapToInt(Integer::intValue).sum();
            milestoneProgress = totalWeight == 0 ? 0 : (int) Math.round(doneWeight * 100.0 / totalWeight);
        }
        int skillProgress = skills.isEmpty() ? milestoneProgress : (int) Math.round(skills.stream().filter(s -> s.getProgress() != null).mapToInt(SkillProgress::getProgress).average().orElse(0));
        AdvancementRoadmap roadmap = roadmapMapper.selectById(roadmapId);
        if (roadmap != null) {
            roadmap.setProgress((milestoneProgress + skillProgress) / 2);
            roadmap.setUpdatedAt(LocalDateTime.now());
            roadmapMapper.updateById(roadmap);
        }
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
            throw new SecurityException("无权限访问该进阶数据");
        }
    }

    private AdvancementRoadmap existingRoadmap(Long id) {
        AdvancementRoadmap roadmap = roadmapMapper.selectById(id);
        if (roadmap == null) {
            throw new IllegalArgumentException("进阶路线不存在");
        }
        return roadmap;
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
