package org.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.usercenter.entity.ResumeEducation;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 教育经历Service接口
 */
public interface ResumeEducationService extends IService<ResumeEducation> {

    /**
     * 新增叫教育经历
     */
    Integer addEducation(ResumeEducation resumeEducation);
}