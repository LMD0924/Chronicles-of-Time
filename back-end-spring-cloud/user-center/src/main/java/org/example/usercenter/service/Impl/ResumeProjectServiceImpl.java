/**
 * 文件说明：拾光记微服务后端用户中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.usercenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.usercenter.entity.ResumeProject;
import org.example.usercenter.mapper.ResumeProjectMapper;
import org.example.usercenter.service.ResumeProjectService;
import org.springframework.stereotype.Service;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 项目经验Service实现类
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Service
@RequiredArgsConstructor
public class ResumeProjectServiceImpl extends ServiceImpl<ResumeProjectMapper, ResumeProject> implements ResumeProjectService {

    private final ResumeProjectMapper resumeProjectMapper;

    /**
     * 新增项目经验
     */
    @Override
    public Integer addProject(ResumeProject resumeProject) {
        return resumeProjectMapper.insert(resumeProject);
    }

}