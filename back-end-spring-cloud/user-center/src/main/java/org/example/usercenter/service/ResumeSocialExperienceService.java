package org.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.usercenter.entity.ResumeSocialExperience;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 社会/校园经历Service接口
 */
public interface ResumeSocialExperienceService extends IService<ResumeSocialExperience> {

    /**
     * 新增社会特长
     */
    Integer addSocialExperience(ResumeSocialExperience resumeSocialExperience);
}