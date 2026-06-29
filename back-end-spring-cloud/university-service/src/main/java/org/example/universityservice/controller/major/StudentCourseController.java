package org.example.universityservice.controller.major;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.universityservice.entity.major.StudentCourse;
import org.example.universityservice.service.major.StudentCourseService;
import org.example.universityservice.vo.major.*;
import org.example.commondb.utils.RestBean;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/student-course")
@RequiredArgsConstructor
public class StudentCourseController {

    private final StudentCourseService studentCourseService;

    @GetMapping("/list")
    public RestBean<List<StudentCourse>> getStudentCourses(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户不存在");
        }
        Long userId = Long.parseLong(userIdStr);
        List<StudentCourse> courses = studentCourseService.getStudentCourses(userId);
        return RestBean.success(courses);
    }

    @GetMapping("/progress")
    public RestBean<GraduationProgressVO> getGraduationProgress(
            @RequestParam Long majorId,
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户不存在");
        }
        Long userId = Long.parseLong(userIdStr);
        GraduationProgressVO progress = studentCourseService.getGraduationProgress(userId, majorId);
        return RestBean.success(progress);
    }

    @PostMapping("/score")
    public RestBean<String> addOrUpdateScore(
            @RequestParam Long courseId,
            @RequestParam BigDecimal score,
            @RequestParam Long majorId,
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户不存在");
        }
        Long userId = Long.parseLong(userIdStr);

        StudentCourse sc = new StudentCourse();
        sc.setUserId(userId);
        sc.setCourseId(courseId);
        sc.setMajorId(majorId);
        sc.setScore(score);
        if (semester != null) {
            sc.setSemester(semester);
        }

        boolean success = studentCourseService.addOrUpdateScore(sc);
        return success ? RestBean.success("保存成功") : RestBean.fail("保存失败");
    }

    @GetMapping("/gap-analysis")
    public RestBean<GraduationGapVO> getGraduationGap(
            @RequestParam Long majorId,
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户不存在");
        }
        Long userId = Long.parseLong(userIdStr);
        return RestBean.success(studentCourseService.getGraduationGap(userId, majorId));
    }

    @GetMapping("/schedule")
    public RestBean<List<SemesterScheduleVO>> getSemesterSchedule(
            @RequestParam Long majorId,
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户不存在");
        }
        Long userId = Long.parseLong(userIdStr);
        return RestBean.success(studentCourseService.getSemesterSchedule(userId, majorId));
    }

    @PostMapping("/simulate-gpa")
    public RestBean<GpaSimulateVO> simulateGpa(
            @RequestBody GpaSimulateRequest body,
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户不存在");
        }
        Long userId = Long.parseLong(userIdStr);
        return RestBean.success(studentCourseService.simulateGpa(userId, body));
    }
}