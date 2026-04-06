/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 学生选课记录控制器
 */
package org.example.highservice.controller;

import jakarta.validation.Valid;
import org.example.commondb.utils.RestBean;
import org.example.highservice.dto.SelectionQueryDTO;
import org.example.highservice.entity.StudentCourseSelection;
import org.example.highservice.service.StudentCourseSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/selection")
public class StudentCourseSelectionController {

    @Autowired
    private StudentCourseSelectionService selectionService;

    /**
     * 学生选课
     */
    @PostMapping("/add")
    public RestBean<?> addSelection(@Valid @RequestBody StudentCourseSelection selection) {
        boolean success = selectionService.selectCourse(selection);
        return success ? RestBean.success("选课成功") : RestBean.fail("选课失败");
    }

    /**
     * 确认选课
     */
    @PutMapping("/confirm/{id}")
    public RestBean<?> confirmSelection(@PathVariable Long id) {
        boolean success = selectionService.confirmSelection(id);
        return success ? RestBean.success("确认成功") : RestBean.fail("确认失败");
    }

    /**
     * 修改选课
     */
    @PutMapping("/modify")
    public RestBean<?> modifySelection(@RequestBody StudentCourseSelection selection) {
        boolean success = selectionService.modifySelection(selection);
        return success ? RestBean.success("修改成功") : RestBean.fail("修改失败");
    }

    /**
     * 退选
     */
    @DeleteMapping("/cancel/{id}")
    public RestBean<?> cancelSelection(@PathVariable Long id, @RequestParam String reason) {
        boolean success = selectionService.cancelSelection(id, reason);
        return success ? RestBean.success("退选成功") : RestBean.fail("退选失败");
    }

    /**
     * 获取学生的选课记录
     */
    @GetMapping("/student/{studentId}")
    public RestBean<?> getStudentSelections(@PathVariable Long studentId) {
        List<StudentCourseSelection> selections = selectionService.getStudentSelections(studentId);
        return RestBean.success(selections);
    }

    /**
     * 获取年级选课统计
     */
    @GetMapping("/statistics/grade")
    public RestBean<?> getGradeStatistics(@RequestParam String grade,
                                          @RequestParam String academicYear) {
        Map<String, Object> stats = selectionService.getGradeStatistics(grade, academicYear);
        return RestBean.success(stats);
    }



    /**
     * 更新公开状态
     */
    @PutMapping("/public/{id}")
    public RestBean<?> updatePublicStatus(@PathVariable Long id, @RequestParam Boolean isPublic) {
        boolean success = selectionService.updatePublicStatus(id, isPublic);
        return success ? RestBean.success("更新成功") : RestBean.fail("更新失败");
    }

    /**
     * 批量更新公开状态
     */
    @PutMapping("/public/batch")
    public RestBean<?> batchUpdatePublicStatus(@RequestBody List<Long> ids, @RequestParam Boolean isPublic) {
        boolean success = selectionService.batchUpdatePublicStatus(ids, isPublic);
        return success ? RestBean.success("批量更新成功") : RestBean.fail("批量更新失败");
    }

    /**
     * 获取公开的选课记录
     */
    @GetMapping("/public")
    public RestBean<?> getPublicSelections() {
        List<StudentCourseSelection> selections = selectionService.getPublicSelections();
        return RestBean.success(selections);
    }

    /**
     * 根据专业推荐选课组合
     */
    @GetMapping("/recommend")
    public RestBean<?> recommendByMajor(@RequestParam(value = "majorName", required = false) String majorName) {
        // 如果专业名称为空，返回默认推荐
        if (majorName == null || majorName.isEmpty()) {
            // 返回热门组合作为默认推荐
            List<Map<String, Object>> hotCombos = selectionService.getHotCombinations();
            return RestBean.success(hotCombos);
        }
        List<Map<String, Object>> recommendations = selectionService.recommendByMajor(majorName);
        return RestBean.success(recommendations);
    }

    /**
     * 生成排名
     */
    @PostMapping("/rank/generate")
    public RestBean<?> generateRank(@RequestParam String grade,
                                    @RequestParam String academicYear,
                                    @RequestParam String semester) {
        selectionService.generateRank(grade, academicYear, semester);
        return RestBean.success("排名生成成功");
    }

    /**
     * 分页查询选课记录（带隐私控制）
     */
    @PostMapping("/query")
    public RestBean<?> querySelections(@RequestBody SelectionQueryDTO queryDTO) {
        List<StudentCourseSelection> selections = selectionService.querySelections(queryDTO);
        return RestBean.success(selections);
    }

    /**
     * 获取热门组合排名
     */
    @GetMapping("/hot-combinations")
    public RestBean<?> getHotCombinations() {
        List<Map<String, Object>> hotCombos = selectionService.getHotCombinations();
        return RestBean.success(hotCombos);
    }

    /**
     * 获取年级排名前N的学生
     */
    @GetMapping("/top-students")
    public RestBean<?> getTopStudents(@RequestParam String grade, @RequestParam(defaultValue = "10") int limit) {
        List<StudentCourseSelection> topStudents = selectionService.getTopStudents(grade, limit);
        return RestBean.success(topStudents);
    }

    /**
     * 获取选课趋势
     */
    @GetMapping("/trend")
    public RestBean<?> getSelectionTrend() {
        List<Map<String, Object>> trend = selectionService.getSelectionTrend();
        return RestBean.success(trend);
    }

    /**
     * 获取学生选课建议
     */
    @GetMapping("/advice/{studentId}")
    public RestBean<?> getSelectionAdvice(@PathVariable Long studentId) {
        Map<String, Object> advice = selectionService.getSelectionAdvice(studentId);
        return RestBean.success(advice);
    }
}