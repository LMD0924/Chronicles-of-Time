/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.service;

import org.example.highservice.entity.SubjectCombination;

import java.util.List;
import java.util.Map;
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

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