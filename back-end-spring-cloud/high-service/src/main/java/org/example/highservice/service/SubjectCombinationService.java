package org.example.highservice.service;

import org.example.highservice.entity.SubjectCombination;

import java.util.List;
import java.util.Map;

public interface SubjectCombinationService {
    /**
     * 获取热门组合排名（基于实际选课数据）
     */
    List<Map<String, Object>> getHotCombinations();

    /**
     * 根据首选科目获取组合
     */
    List<SubjectCombination> getCombinationsByFirstSubject(String firstSubject);

    /**
     * 获取所有组合及其详细信息
     */
    List<Map<String, Object>> getAllCombinationsWithDetails();

    /**
     * 根据科目ID查找包含该科目的组合
     */
    List<SubjectCombination> getCombinationsBySubject(Long subjectId);

    /**
     * 获取组合的选课人数统计
     */
    List<Map<String, Object>> getCombinationStudentCount();
}