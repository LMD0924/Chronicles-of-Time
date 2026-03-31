package org.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.usercenter.entity.ResumeProject;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 项目经验Service接口
 */
public interface ResumeProjectService extends IService<ResumeProject> {

    /**
     * 新增项目经验
     */
    Integer addProject(ResumeProject resumeProject);
}