package org.example.usercenter.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2026/3/30
 * @Description: 完整简历响应实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeComplete {

    // 简历主表信息
    private Resume resume;

    // 教育经历列表
    private List<ResumeEducation> educationList;

    // 工作经历列表
    private List<ResumeWorkExperience> workExperienceList;

    // 项目经验列表
    private List<ResumeProject> projectList;

    // 技能特长列表
    private List<ResumeSkill> skillList;

    // 证书列表
    private List<ResumeCertificate> certificateList;

    // 社会经历列表
    private List<ResumeSocialExperience> socialExperienceList;

}