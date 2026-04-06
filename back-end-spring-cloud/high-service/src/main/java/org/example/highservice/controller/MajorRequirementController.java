/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 大学专业选科要求控制器
 */
package org.example.highservice.controller;

import org.example.commondb.utils.RestBean;
import org.example.highservice.entity.MajorRequirement;
import org.example.highservice.entity.MajorSubjectMatching;
import org.example.highservice.service.MajorRequirementService;
import org.example.highservice.service.MajorSubjectMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/major")
public class MajorRequirementController {

    @Autowired
    private MajorRequirementService majorRequirementService;
    @Autowired
    private MajorSubjectMatchingService majorSubjectMatchingService;

    /**
     * 根据选科组合匹配专业
     */
    @GetMapping("/match")
    public RestBean<?> matchMajorByCombination(@RequestParam String firstSubject,
                                               @RequestParam Long subject1Id,
                                               @RequestParam Long subject2Id,
                                               @RequestParam Long subject3Id,
                                               @RequestParam(defaultValue = "10") int limit) {
        List<MajorRequirement> majors = majorRequirementService.matchMajorByCombination(
                firstSubject, subject1Id, subject2Id, subject3Id, limit);
        return RestBean.success(majors);
    }

    /**
     * 获取热门专业
     */
    @GetMapping("/hot")
    public RestBean<?> getHotMajors(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> hotMajors = majorRequirementService.getHotMajors(limit);
        return RestBean.success(hotMajors);
    }

    /**
     * 根据大学层次获取专业
     */
    @GetMapping("/level/{level}")
    public RestBean<?> getMajorsByUniversityLevel(@PathVariable String level) {
        List<MajorRequirement> majors = majorRequirementService.getMajorsByUniversityLevel(level);
        return RestBean.success(majors);
    }

    /**
     * 获取专业详情
     */
    @GetMapping("/detail/{majorCode}")
    public RestBean<?> getMajorDetail(@PathVariable String majorCode) {
        Map<String, Object> detail = majorRequirementService.getMajorDetail(majorCode);
        return RestBean.success(detail);
    }

    /**
     * 获取专业类别统计
     */
    @GetMapping("/category/statistics")
    public RestBean<?> getCategoryStatistics() {
        Map<String, Object> statistics = majorRequirementService.getCategoryStatistics();
        return RestBean.success(statistics);
    }

    /**
     * 搜索专业
     */
    @GetMapping("/search")
    public RestBean<?> searchMajors(@RequestParam String keyword) {
        List<MajorRequirement> majors = majorRequirementService.searchMajors(keyword);
        return RestBean.success(majors);
    }

    @GetMapping("/list")
    public RestBean<?> list(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return RestBean.success(majorRequirementService.list());
        }
        return RestBean.success(majorRequirementService.searchMajors(keyword));
    }

    @GetMapping("/detailById/{id}")
    public RestBean<?> detailById(@PathVariable Long id) {
        return RestBean.success(majorRequirementService.getById(id));
    }

    @PostMapping("/save")
    public RestBean<?> saveOrUpdate(@RequestBody MajorRequirement majorRequirement) {
        boolean ok = majorRequirement.getId() == null
                ? majorRequirementService.save(majorRequirement)
                : majorRequirementService.updateById(majorRequirement);
        return ok ? RestBean.success("保存成功", majorRequirement) : RestBean.fail("保存失败");
    }

    @PostMapping("/delete")
    public RestBean<?> delete(@RequestParam Long id) {
        return RestBean.success(majorRequirementService.removeById(id) ? 1 : 0);
    }

    @GetMapping("/matching/list")
    public RestBean<?> matchingList(@RequestParam(required = false) String majorCode) {
        if (majorCode == null || majorCode.trim().isEmpty()) {
            return RestBean.success(majorSubjectMatchingService.list());
        }
        return RestBean.success(
                majorSubjectMatchingService.lambdaQuery()
                        .eq(MajorSubjectMatching::getMajorCode, majorCode)
                        .orderByAsc(MajorSubjectMatching::getImportanceLevel)
                        .list()
        );
    }

    @GetMapping("/matching/detail/{id}")
    public RestBean<?> matchingDetail(@PathVariable Long id) {
        return RestBean.success(majorSubjectMatchingService.getById(id));
    }

    @PostMapping("/matching/save")
    public RestBean<?> matchingSave(@RequestBody MajorSubjectMatching matching) {
        boolean ok = matching.getId() == null
                ? majorSubjectMatchingService.save(matching)
                : majorSubjectMatchingService.updateById(matching);
        return ok ? RestBean.success("保存成功", matching) : RestBean.fail("保存失败");
    }

    @PostMapping("/matching/delete")
    public RestBean<?> matchingDelete(@RequestParam Long id) {
        return RestBean.success(majorSubjectMatchingService.removeById(id) ? 1 : 0);
    }
}