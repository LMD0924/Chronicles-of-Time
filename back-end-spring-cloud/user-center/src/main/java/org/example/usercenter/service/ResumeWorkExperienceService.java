package org.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.usercenter.entity.ResumeWorkExperience;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 工作经历Service接口
 */
public interface ResumeWorkExperienceService extends IService<ResumeWorkExperience> {

    /**
     * 新增工作经历
     */
    Integer addWorkExperience(ResumeWorkExperience resumeWorkExperience);
}