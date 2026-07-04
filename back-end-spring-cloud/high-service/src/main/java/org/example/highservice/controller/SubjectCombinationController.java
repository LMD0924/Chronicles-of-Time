/**
 * 文件说明：拾光记微服务后端高中服务接口控制器源码，负责接口控制器相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.controller;

import jakarta.annotation.Resource;
import org.example.highservice.entity.SubjectCombination;
import org.example.highservice.service.SubjectCombinationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 类说明：当前类是接口控制器模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@RestController
@RequestMapping("api/subject-combination")
public class SubjectCombinationController {

    @Resource
    private SubjectCombinationService subjectCombinationService;

    /**
     * 获取热门组合排名（基于实际选课数据）
     */
    @GetMapping("/hot")
    public List<Map<String, Object>> getHotCombinations() {
        return subjectCombinationService.getHotCombinations();
    }

    /**
     * 根据首选科目获取组合
     */
    @GetMapping("/by-first-subject/{firstSubject}")
    public List<SubjectCombination> getCombinationsByFirstSubject(@PathVariable String firstSubject) {
        return subjectCombinationService.getCombinationsByFirstSubject(firstSubject);
    }

    /**
     * 获取所有组合及其详细信息
     */
    @GetMapping("/all")
    public List<Map<String, Object>> getAllCombinationsWithDetails() {
        return subjectCombinationService.getAllCombinationsWithDetails();
    }

    /**
     * 根据科目ID查找包含该科目的组合
     */
    @GetMapping("/by-subject/{subjectId}")
    public List<SubjectCombination> getCombinationsBySubject(@PathVariable Long subjectId) {
        return subjectCombinationService.getCombinationsBySubject(subjectId);
    }

    /**
     * 获取组合的选课人数统计
     */
    @GetMapping("/student-count")
    public List<Map<String, Object>> getCombinationStudentCount() {
        return subjectCombinationService.getCombinationStudentCount();
    }
}