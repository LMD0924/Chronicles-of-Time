package org.example.usercenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.usercenter.entity.*;
import org.example.usercenter.mapper.ResumeMapper;
import org.example.usercenter.service.*;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    private final ResumeMapper resumeMapper;
    private final ResumeEducationService resumeEducationService;
    private final ResumeWorkExperienceService resumeWorkExperienceService;
    private final ResumeProjectService resumeProjectService;
    private final ResumeSkillService resumeSkillService;
    private final ResumeCertificateService resumeCertificateService;
    private final ResumeSocialExperienceService resumeSocialExperienceService;

    /**
     * 新增简历主表
     */
    @Override
    public Integer addResume(Resume resume){
        return resumeMapper.insert(resume);
    }

    /**
     * 根据userId获取完整简历信息
     */
    @Override
    public ResumeComplete getCompleteResumeByUserId(Long userId) {
        // 查询简历主表
        QueryWrapper<Resume> resumeQueryWrapper = new QueryWrapper<>();
        resumeQueryWrapper.eq("user_id", userId);
        Resume resume = resumeMapper.selectOne(resumeQueryWrapper);

        if (resume == null) {
            return new ResumeComplete();
        }

        Long resumeId = resume.getId();

        // 查询教育经历
        QueryWrapper<ResumeEducation> educationQueryWrapper = new QueryWrapper<>();
        educationQueryWrapper.eq("resume_id", resumeId).orderByAsc("sort_order");
        List<ResumeEducation> educationList = resumeEducationService.list(educationQueryWrapper);

        // 查询工作经历
        QueryWrapper<ResumeWorkExperience> workExperienceQueryWrapper = new QueryWrapper<>();
        workExperienceQueryWrapper.eq("resume_id", resumeId).orderByAsc("sort_order");
        List<ResumeWorkExperience> workExperienceList = resumeWorkExperienceService.list(workExperienceQueryWrapper);

        // 查询项目经验
        QueryWrapper<ResumeProject> projectQueryWrapper = new QueryWrapper<>();
        projectQueryWrapper.eq("resume_id", resumeId).orderByAsc("sort_order");
        List<ResumeProject> projectList = resumeProjectService.list(projectQueryWrapper);

        // 查询技能特长
        QueryWrapper<ResumeSkill> skillQueryWrapper = new QueryWrapper<>();
        skillQueryWrapper.eq("resume_id", resumeId).orderByAsc("sort_order");
        List<ResumeSkill> skillList = resumeSkillService.list(skillQueryWrapper);

        // 查询证书
        QueryWrapper<ResumeCertificate> certificateQueryWrapper = new QueryWrapper<>();
        certificateQueryWrapper.eq("resume_id", resumeId);
        List<ResumeCertificate> certificateList = resumeCertificateService.list(certificateQueryWrapper);

        // 查询社会经历
        QueryWrapper<ResumeSocialExperience> socialExperienceQueryWrapper = new QueryWrapper<>();
        socialExperienceQueryWrapper.eq("resume_id", resumeId).orderByAsc("sort_order");
        List<ResumeSocialExperience> socialExperienceList = resumeSocialExperienceService.list(socialExperienceQueryWrapper);

        // 组装完整简历
        return new ResumeComplete(
                resume,
                educationList,
                workExperienceList,
                projectList,
                skillList,
                certificateList,
                socialExperienceList
        );
    }

    /**
     * 根据userId检查简历是否存在
     */
    @Override
    public boolean existsByUserId(Long userId) {
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return resumeMapper.exists(queryWrapper);
    }

    /**
     * 根据 userId 获取该用户的简历主表（如果存在）
     */
    @Override
    public Resume getByUserId(Long userId) {
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return resumeMapper.selectOne(queryWrapper);
    }
}
