/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 科目基础信息Service接口
 */
package org.example.highservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.highservice.entity.Subject;

import java.util.List;
import java.util.Map;

public interface SubjectService extends IService<Subject> {

    /**
     * 获取所有启用的科目
     */
    List<Subject> getAllActiveSubjects();

    /**
     * 获取必考科目
     */
    List<Subject> getRequiredSubjects();

    /**
     * 获取首选科目
     */
    List<Subject> getFirstSubjects();

    /**
     * 获取再选科目
     */
    List<Subject> getSecondSubjects();

    /**
     * 根据类别获取科目
     */
    List<Subject> getSubjectsByCategory(Integer category);

    /**
     * 获取科目选课人数统计
     */
    Map<String, Object> getSubjectSelectionCount();

    /**
     * 批量启用/禁用科目
     */
    boolean batchUpdateStatus(List<Long> ids, Boolean isActive);
}