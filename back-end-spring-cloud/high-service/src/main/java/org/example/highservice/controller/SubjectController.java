/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 科目基础信息控制器
 */
package org.example.highservice.controller;

import org.example.commondb.utils.RestBean;
import org.example.highservice.entity.Subject;
import org.example.highservice.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subject")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    /**
     * 获取所有启用的科目
     */
    @GetMapping("/all")
    public RestBean<?> getAllActiveSubjects() {
        List<Subject> subjects = subjectService.getAllActiveSubjects();
        return RestBean.success(subjects);
    }

    /**
     * 获取必考科目
     */
    @GetMapping("/required")
    public RestBean<?> getRequiredSubjects() {
        List<Subject> subjects = subjectService.getRequiredSubjects();
        return RestBean.success(subjects);
    }

    /**
     * 获取首选科目（物理/历史）
     */
    @GetMapping("/first")
    public RestBean<?> getFirstSubjects() {
        List<Subject> subjects = subjectService.getFirstSubjects();
        return RestBean.success(subjects);
    }

    /**
     * 获取再选科目（化学/生物/政治/地理）
     */
    @GetMapping("/second")
    public RestBean<?> getSecondSubjects() {
        List<Subject> subjects = subjectService.getSecondSubjects();
        return RestBean.success(subjects);
    }

    /**
     * 根据类别获取科目
     */
    @GetMapping("/category/{category}")
    public RestBean<?> getSubjectsByCategory(@PathVariable Integer category) {
        List<Subject> subjects = subjectService.getSubjectsByCategory(category);
        return RestBean.success(subjects);
    }

    /**
     * 获取科目选课人数统计
 * 该接口用于获取各科目的选课人数统计信息
 * @return 返回一个包含统计数据的RestBean对象，状态码为200表示成功
     */
    @GetMapping("/statistics")  // HTTP GET请求映射，访问路径为/statistics
    public RestBean<?> getSubjectSelectionCount() {  // 方法名表明获取科目选课数量，返回类型为泛型的RestBean
        Map<String, Object> statistics = subjectService.getSubjectSelectionCount();  // 调用服务层方法获取科目选课统计信息
        return RestBean.success(statistics);  // 封装统计数据到RestBean中并返回成功响应
    }
}