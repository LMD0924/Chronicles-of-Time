/**
 * 文件说明：拾光记微服务后端用户中心数据实体源码，负责数据实体相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是数据实体模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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