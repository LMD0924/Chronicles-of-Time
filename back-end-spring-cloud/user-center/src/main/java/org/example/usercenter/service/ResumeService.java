package org.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.usercenter.entity.Resume;
import org.example.usercenter.entity.ResumeComplete;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description:
 */
public interface ResumeService extends IService<Resume> {

    /**
     * 新增简历主表
     */
    Integer addResume(Resume resume);

    /**
     * 根据userId获取完整简历信息
     */
    ResumeComplete getCompleteResumeByUserId(Long userId);

    /**
     * 根据 userId 获取该用户的简历主表（如果存在）
     */
    Resume getByUserId(Long userId);

    /**
     * 根据userId检查简历是否存在
     */
    boolean existsByUserId(Long userId);
}
