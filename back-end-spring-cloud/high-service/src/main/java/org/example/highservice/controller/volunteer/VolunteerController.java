package org.example.highservice.controller.volunteer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.highservice.entity.volunteer.*;
import org.example.highservice.service.volunteer.VolunteerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 志愿填报控制器
 */
@Slf4j
@RestController
@RequestMapping("api/volunteer")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    // ==================== 志愿方案管理 ====================

    /**
     * 保存志愿方案
     */
    @PostMapping("/plan/save")
    public RestBean<String> saveVolunteerPlan(@RequestBody UserVolunteer userVolunteer) {
        log.info("========== 保存志愿方案 ==========");
        if (volunteerService.saveVolunteerPlan(userVolunteer)) {
            return RestBean.success("保存成功");
        }
        return RestBean.fail("保存失败");
    }

    /**
     * 更新志愿方案
     */
    @PutMapping("/plan/update")
    public RestBean<String> updateVolunteerPlan(@RequestBody UserVolunteer userVolunteer) {
        log.info("========== 更新志愿方案 ==========");
        if (volunteerService.updateVolunteerPlan(userVolunteer)) {
            return RestBean.success("更新成功");
        }
        return RestBean.fail("更新失败");
    }

    /**
     * 删除志愿方案
     */
    @DeleteMapping("/plan/delete/{id}")
    public RestBean<String> deleteVolunteerPlan(@PathVariable Integer id) {
        log.info("========== 删除志愿方案 ==========");
        if (volunteerService.deleteVolunteerPlan(id)) {
            return RestBean.success("删除成功");
        }
        return RestBean.fail("删除失败");
    }

    /**
     * 获取志愿方案详情
     */
    @GetMapping("/plan/{id}")
    public RestBean<UserVolunteer> getVolunteerPlan(@PathVariable Integer id) {
        log.info("========== 获取志愿方案详情 ==========");
        UserVolunteer plan = volunteerService.getVolunteerPlanById(id);
        return RestBean.success(plan);
    }

    /**
     * 获取用户的所有志愿方案
     */
    @GetMapping("/plan/list/{userId}")
    public RestBean<List<UserVolunteer>> getUserVolunteerPlans(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer year) {
        log.info("========== 获取用户志愿方案列表 ==========");
        List<UserVolunteer> list = volunteerService.getUserVolunteerPlans(userId, year);
        return RestBean.success(list);
    }

    // ==================== 志愿详情管理 ====================

    /**
     * 添加志愿详情
     */
    @PostMapping("/detail/add")
    public RestBean<String> addVolunteerDetail(@RequestBody VolunteerDetail volunteerDetail) {
        log.info("========== 添加志愿详情 ==========");
        if (volunteerService.addVolunteerDetail(volunteerDetail)) {
            return RestBean.success("添加成功");
        }
        return RestBean.fail("添加失败");
    }

    /**
     * 批量添加志愿详情
     */
    @PostMapping("/detail/batchAdd")
    public RestBean<String> batchAddVolunteerDetails(@RequestBody List<VolunteerDetail> details) {
        log.info("========== 批量添加志愿详情 ==========");
        if (volunteerService.batchAddVolunteerDetails(details)) {
            return RestBean.success("批量添加成功");
        }
        return RestBean.fail("批量添加失败");
    }

    /**
     * 更新志愿详情
     */
    @PutMapping("/detail/update")
    public RestBean<String> updateVolunteerDetail(@RequestBody VolunteerDetail volunteerDetail) {
        log.info("========== 更新志愿详情 ==========");
        if (volunteerService.updateVolunteerDetail(volunteerDetail)) {
            return RestBean.success("更新成功");
        }
        return RestBean.fail("更新失败");
    }

    /**
     * 删除志愿详情
     */
    @DeleteMapping("/detail/delete/{id}")
    public RestBean<String> deleteVolunteerDetail(@PathVariable Integer id) {
        log.info("========== 删除志愿详情 ==========");
        if (volunteerService.deleteVolunteerDetail(id)) {
            return RestBean.success("删除成功");
        }
        return RestBean.fail("删除失败");
    }

    /**
     * 获取志愿详情列表
     */
    @GetMapping("/detail/list/{volunteerId}")
    public RestBean<List<VolunteerDetail>> getVolunteerDetails(@PathVariable Integer volunteerId) {
        log.info("========== 获取志愿详情列表 ==========");
        List<VolunteerDetail> list = volunteerService.getVolunteerDetails(volunteerId);
        return RestBean.success(list);
    }

    // ==================== 智能推荐 ====================

    /**
     * 智能推荐大学
     */
    @PostMapping("/recommend/universities")
    public RestBean<List<Map<String, Object>>> recommendUniversities(
            @RequestParam Long userId,
            @RequestParam Integer year,
            @RequestParam String province,
            @RequestParam Integer score,
            @RequestParam Integer rank,
            @RequestBody(required = false) List<String> subjects) {
        log.info("========== 智能推荐大学 ==========");
        List<Map<String, Object>> recommendations = volunteerService.recommendUniversities(
                userId, year, province, score, rank, subjects);
        return RestBean.success(recommendations);
    }

    /**
     * 按专业推荐
     */
    @GetMapping("/recommend/byMajor")
    public RestBean<List<Map<String, Object>>> recommendByMajor(
            @RequestParam Long userId,
            @RequestParam String majorCode,
            @RequestParam Integer score,
            @RequestParam String province) {
        log.info("========== 按专业推荐 ==========");
        List<Map<String, Object>> recommendations = volunteerService.recommendByMajor(
                userId, majorCode, score, province);
        return RestBean.success(recommendations);
    }

    // ==================== 匹配度分析 ====================

    /**
     * 检查选科匹配度
     */
    @PostMapping("/matching/check/{detailId}")
    public RestBean<Map<String, Object>> checkSubjectMatching(
            @PathVariable Integer detailId,
            @RequestBody List<String> selectedSubjects) {
        log.info("========== 检查选科匹配度 ==========");
        Map<String, Object> result = volunteerService.checkSubjectMatching(detailId, selectedSubjects);
        return RestBean.success(result);
    }

    /**
     * 获取志愿匹配报告
     */
    @GetMapping("/matching/report/{volunteerId}")
    public RestBean<List<Map<String, Object>>> getVolunteerMatchingReport(
            @PathVariable Integer volunteerId,
            @RequestParam List<String> selectedSubjects) {
        log.info("========== 获取志愿匹配报告 ==========");
        List<Map<String, Object>> report = volunteerService.getVolunteerMatchingReport(
                volunteerId, selectedSubjects);
        return RestBean.success(report);
    }

    // ==================== 模拟录取 ====================

    /**
     * 模拟单个志愿录取
     */
    @PostMapping("/simulate/single/{detailId}")
    public RestBean<AdmissionSimulation> simulateAdmission(@PathVariable Integer detailId) {
        log.info("========== 模拟单个志愿录取 ==========");
        AdmissionSimulation result = volunteerService.simulateAdmission(detailId);
        return RestBean.success(result);
    }

    /**
     * 批量模拟录取
     */
    @GetMapping("/simulate/batch/{volunteerId}")
    public RestBean<List<Map<String, Object>>> batchSimulateAdmission(@PathVariable Integer volunteerId) {
        log.info("========== 批量模拟录取 ==========");
        List<Map<String, Object>> results = volunteerService.batchSimulateAdmission(volunteerId);
        return RestBean.success(results);
    }

    /**
     * 获取录取分析报告
     */
    @GetMapping("/simulate/analysis/{volunteerId}")
    public RestBean<Map<String, Object>> getAdmissionAnalysis(@PathVariable Integer volunteerId) {
        log.info("========== 获取录取分析报告 ==========");
        Map<String, Object> analysis = volunteerService.getAdmissionAnalysis(volunteerId);
        return RestBean.success(analysis);
    }

    // ==================== 统计分析 ====================

    /**
     * 获取志愿统计
     */
    @GetMapping("/statistics/{userId}")
    public RestBean<List<Map<String, Object>>> getVolunteerStatistics(@PathVariable Integer userId) {
        log.info("========== 获取志愿统计 ==========");
        List<Map<String, Object>> statistics = volunteerService.getVolunteerStatistics(userId);
        return RestBean.success(statistics);
    }

    /**
     * 获取录取机会分析
     */
    @GetMapping("/chance/{userId}")
    public RestBean<Map<String, Object>> getApplicationChance(
            @PathVariable Integer userId,
            @RequestParam Integer universityId,
            @RequestParam Integer majorId) {
        log.info("========== 获取录取机会分析 ==========");
        Map<String, Object> chance = volunteerService.getApplicationChance(userId, universityId, majorId);
        return RestBean.success(chance);
    }

    // ==================== 数据查询 ====================

    /**
     * 搜索大学
     */
    @GetMapping("/search/universities")
    public RestBean<List<University>> searchUniversities(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String level) {
        log.info("========== 搜索大学 ==========");
        List<University> list = volunteerService.searchUniversities(keyword, province, level);
        return RestBean.success(list);
    }

    /**
     * 搜索专业
     */
    @GetMapping("/search/majors")
    public RestBean<List<Major>> searchMajors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer universityId) {
        log.info("========== 搜索专业 ==========");
        List<Major> list = volunteerService.searchMajors(keyword, category, universityId);
        return RestBean.success(list);
    }

    /**
     * 获取招生历史数据
     */
    @GetMapping("/admission/history")
    public RestBean<List<AdmissionPlan>> getAdmissionHistory(
            @RequestParam(required = false) Integer majorId,
            @RequestParam(required = false) Integer universityId,
            @RequestParam(required = false) Integer year) {
        log.info("========== 获取招生历史数据 ==========");
        List<AdmissionPlan> list = volunteerService.getAdmissionHistory(majorId, universityId, year);
        return RestBean.success(list);
    }
    
    /**
     * 获取所有省份列表
     */
    @GetMapping("/filter/provinces")
    public RestBean<List<String>> getAllProvinces() {
        log.info("========== 获取所有省份列表 ==========");
        List<String> list = volunteerService.getAllProvinces();
        return RestBean.success(list);
    }
    
    /**
     * 获取所有大学层次列表
     */
    @GetMapping("/filter/levels")
    public RestBean<List<String>> getAllUniversityLevels() {
        log.info("========== 获取所有大学层次列表 ==========");
        List<String> list = volunteerService.getAllUniversityLevels();
        return RestBean.success(list);
    }
    
    /**
     * 获取所有学科门类列表
     */
    @GetMapping("/filter/categories")
    public RestBean<List<String>> getAllCategories() {
        log.info("========== 获取所有学科门类列表 ==========");
        List<String> list = volunteerService.getAllCategories();
        return RestBean.success(list);
    }
}