/**
 * 文件说明：拾光记微服务后端用户中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.usercenter.entity.ResumeWorkExperience;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 工作经历Service接口
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
public interface ResumeWorkExperienceService extends IService<ResumeWorkExperience> {

    /**
     * 新增工作经历
     */
    Integer addWorkExperience(ResumeWorkExperience resumeWorkExperience);
}