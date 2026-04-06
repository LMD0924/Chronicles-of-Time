/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 等级赋分控制器
 */
package org.example.highservice.controller;

import org.example.commondb.utils.RestBean;
import org.example.highservice.entity.GradingScale;
import org.example.highservice.service.GradingScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/grading")
public class GradingScaleController {

    @Autowired
    private GradingScaleService gradingScaleService;

    /**
     * 获取科目的赋分等级配置
     */
    @GetMapping("/scale/{subjectId}")
    public RestBean<?> getScalesBySubject(@PathVariable Long subjectId,
                                          @RequestParam String academicYear) {
        List<GradingScale> scales = gradingScaleService.getScalesBySubject(subjectId, academicYear);
        return RestBean.success(scales);
    }

    /**
     * 根据原始分计算赋分
     */
    @GetMapping("/calculate")
    public RestBean<?> calculateWeightedScore(@RequestParam Long subjectId,
                                              @RequestParam BigDecimal rawScore,
                                              @RequestParam String academicYear) {
        BigDecimal weightedScore = gradingScaleService.calculateWeightedScore(subjectId, rawScore, academicYear);
        return RestBean.success(weightedScore);
    }

    /**
     * 添加赋分等级配置
     */
    @PostMapping("/add")
    public RestBean<?> addGradingScale(@RequestBody GradingScale scale) {
        boolean success = gradingScaleService.save(scale);
        return success ? RestBean.success("添加成功") : RestBean.fail("添加失败");
    }

    /**
     * 批量更新赋分等级状态
     */
    @PutMapping("/status/batch")
    public RestBean<?> batchUpdateStatus(@RequestParam String academicYear,
                                         @RequestParam Boolean isActive) {
        boolean success = gradingScaleService.batchUpdateStatus(academicYear, isActive);
        return success ? RestBean.success("更新成功") : RestBean.fail("更新失败");
    }

    /**
     * 获取所有有效的赋分配置
     */
    @GetMapping("/active")
    public RestBean<?> getActiveScales() {
        List<GradingScale> scales = gradingScaleService.getActiveScales();
        return RestBean.success(scales);
    }

    @GetMapping("/list")
    public RestBean<?> listByYear(@RequestParam(required = false) String academicYear) {
        if (academicYear == null || academicYear.trim().isEmpty()) {
            return RestBean.success(gradingScaleService.list());
        }
        return RestBean.success(
                gradingScaleService.lambdaQuery()
                        .eq(GradingScale::getAcademicYear, academicYear)
                        .orderByAsc(GradingScale::getSubjectId)
                        .orderByAsc(GradingScale::getRawScoreMin)
                        .list()
        );
    }

    @GetMapping("/detail/{id}")
    public RestBean<?> detail(@PathVariable Long id) {
        return RestBean.success(gradingScaleService.getById(id));
    }

    @PostMapping("/save")
    public RestBean<?> saveOrUpdate(@RequestBody GradingScale scale) {
        boolean ok = scale.getId() == null ? gradingScaleService.save(scale) : gradingScaleService.updateById(scale);
        return ok ? RestBean.success("保存成功", scale) : RestBean.fail("保存失败");
    }

    @PostMapping("/delete")
    public RestBean<?> delete(@RequestParam Long id) {
        return RestBean.success(gradingScaleService.removeById(id) ? 1 : 0);
    }

    @GetMapping("/distribution")
    public RestBean<?> distribution() {
        return RestBean.success(gradingScaleService.getScoreDistribution());
    }
}