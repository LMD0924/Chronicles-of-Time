/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.service.major.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.universityservice.entity.major.Course;
import org.example.universityservice.entity.major.GraduationRequirement;
import org.example.universityservice.entity.major.Major;
import org.example.universityservice.entity.major.StudentCourse;
import org.example.universityservice.mapper.major.GraduationRequirementMapper;
import org.example.universityservice.service.major.CourseService;
import org.example.universityservice.service.major.GraduationRequirementService;
import org.example.universityservice.service.major.MajorService;
import org.example.universityservice.service.major.StudentCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
public class GraduationRequirementServiceImpl extends ServiceImpl<GraduationRequirementMapper, GraduationRequirement> implements GraduationRequirementService {

    private final GraduationRequirementMapper graduationRequirementMapper;
    private final MajorService majorService;
    private CourseService courseService;
    private StudentCourseService studentCourseService;

    public GraduationRequirementServiceImpl(GraduationRequirementMapper graduationRequirementMapper,
                                            MajorService majorService) {
        this.graduationRequirementMapper = graduationRequirementMapper;
        this.majorService = majorService;
    }

    @Autowired
    @Lazy
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    @Autowired
    @Lazy
    public void setStudentCourseService(StudentCourseService studentCourseService) {
        this.studentCourseService = studentCourseService;
    }

    @Override
    public GraduationRequirement getByUserIdAndMajorId(Long userId, Long majorId) {
        LambdaQueryWrapper<GraduationRequirement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GraduationRequirement::getUserId, userId)
                .eq(GraduationRequirement::getMajorId, majorId);
        return this.getOne(wrapper);
    }

    @Override
    public boolean saveOrUpdate(GraduationRequirement requirement) {
        if (requirement.getId() == null) {
            return this.save(requirement);
        } else {
            return this.updateById(requirement);
        }
    }

    @Override
    public void calculateProgress(Long userId, Long majorId) {
        // 获取专业信息
        Major major = majorService.getMajorById(majorId);
        if (major == null) {
            log.error("专业不存在: {}", majorId);
            return;
        }

        // 获取学生所有已通过的课程
        List<StudentCourse> studentCourses = studentCourseService.lambdaQuery()
                .eq(StudentCourse::getUserId, userId)
                .eq(StudentCourse::getMajorId, majorId)
                .eq(StudentCourse::getIsPassed, 1)
                .list();

        if (studentCourses.isEmpty()) {
            // 没有通过的课程，创建空记录
            GraduationRequirement existing = getByUserIdAndMajorId(userId, majorId);
            if (existing == null) {
                GraduationRequirement newReq = new GraduationRequirement();
                newReq.setUserId(userId);
                newReq.setMajorId(majorId);
                newReq.setTotalCreditsRequired(major.getTotalCredits());
                newReq.setCompulsoryCreditsRequired(major.getCompulsoryCredits());
                newReq.setElectiveCreditsRequired(major.getElectiveCredits());
                newReq.setTotalCreditsEarned(0);
                newReq.setCompulsoryCreditsEarned(0);
                newReq.setElectiveCreditsEarned(0);
                newReq.setGpa(BigDecimal.ZERO);
                newReq.setProgressPercent(0);
                newReq.setStatus("studying");
                this.save(newReq);
            }
            return;
        }

        // 获取课程信息
        List<Long> courseIds = studentCourses.stream()
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toList());

        List<Course> courses = courseService.listByIds(courseIds);
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        // 计算已修学分
        int totalEarned = 0;
        int compulsoryEarned = 0;
        int electiveEarned = 0;

        for (StudentCourse sc : studentCourses) {
            Course course = courseMap.get(sc.getCourseId());
            if (course != null) {
                int credit = course.getCredit().intValue();
                totalEarned += credit;
                if ("compulsory".equals(course.getCourseType())) {
                    compulsoryEarned += credit;
                } else {
                    electiveEarned += credit;
                }
            }
        }

        // 计算平均绩点
        BigDecimal totalGp = BigDecimal.ZERO;
        int validCount = 0;
        for (StudentCourse sc : studentCourses) {
            if (sc.getGradePoint() != null && sc.getGradePoint().compareTo(BigDecimal.ZERO) > 0) {
                totalGp = totalGp.add(sc.getGradePoint());
                validCount++;
            }
        }
        BigDecimal avgGpa = validCount > 0
                ? totalGp.divide(BigDecimal.valueOf(validCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 计算进度百分比
        int progressPercent = (int) ((double) totalEarned / major.getTotalCredits() * 100);
        if (progressPercent > 100) progressPercent = 100;

        // 确定学业状态
        String status;
        if (totalEarned >= major.getTotalCredits()) {
            status = "completed";
        } else {
            status = "studying";
        }

        // 更新或创建毕业要求记录
        GraduationRequirement requirement = getByUserIdAndMajorId(userId, majorId);
        if (requirement == null) {
            requirement = new GraduationRequirement();
            requirement.setUserId(userId);
            requirement.setMajorId(majorId);
            requirement.setTotalCreditsRequired(major.getTotalCredits());
            requirement.setCompulsoryCreditsRequired(major.getCompulsoryCredits());
            requirement.setElectiveCreditsRequired(major.getElectiveCredits());
        }

        requirement.setTotalCreditsEarned(totalEarned);
        requirement.setCompulsoryCreditsEarned(compulsoryEarned);
        requirement.setElectiveCreditsEarned(electiveEarned);
        requirement.setGpa(avgGpa);
        requirement.setProgressPercent(progressPercent);
        requirement.setStatus(status);

        this.saveOrUpdate(requirement);

        log.info("更新毕业进度 - 用户: {}, 专业: {}, 已修学分: {}/{}, 进度: {}%",
                userId, majorId, totalEarned, major.getTotalCredits(), progressPercent);
    }
}