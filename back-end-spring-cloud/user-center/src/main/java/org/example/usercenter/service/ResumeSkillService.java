package org.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.usercenter.entity.ResumeSkill;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 技能特长Service接口
 */
public interface ResumeSkillService extends IService<ResumeSkill> {

    /**
     * 新增技能特长
     */
    Integer addSkill(ResumeSkill resumeSkill);
}