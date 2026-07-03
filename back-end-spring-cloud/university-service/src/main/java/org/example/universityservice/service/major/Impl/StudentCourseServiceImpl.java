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
import org.example.universityservice.mapper.major.StudentCourseMapper;
import org.example.universityservice.service.major.CourseService;
import org.example.universityservice.service.major.GraduationRequirementService;
import org.example.universityservice.service.major.MajorService;
import org.example.universityservice.service.major.StudentCourseService;
import org.example.universityservice.vo.major.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
public class StudentCourseServiceImpl extends ServiceImpl<StudentCourseMapper, StudentCourse> implements StudentCourseService {

    private final StudentCourseMapper studentCourseMapper;
    private final MajorService majorService;
    private final GraduationRequirementService graduationRequirementService;
    private CourseService courseService;  // 不是 final

    // 构造函数注入非循环依赖的 Bean
    public StudentCourseServiceImpl(StudentCourseMapper studentCourseMapper,
                                    MajorService majorService,
                                    GraduationRequirementService graduationRequirementService) {
        this.studentCourseMapper = studentCourseMapper;
        this.majorService = majorService;
        this.graduationRequirementService = graduationRequirementService;
    }

    // Setter 注入循环依赖的 Bean，使用 @Lazy
    @Autowired
    @Lazy
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    @Override
    public List<StudentCourse> getStudentCourses(Long userId) {
        return studentCourseMapper.selectByUserId(userId);
    }

    @Override
    public List<StudentCourse> getStudentCoursesByMajor(Long userId, Long majorId) {
        return studentCourseMapper.selectByUserIdAndMajorId(userId, majorId);
    }

    @Override
    public StudentCourse getStudentCourse(Long userId, Long courseId) {
        LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourse::getUserId, userId)
                .eq(StudentCourse::getCourseId, courseId);
        return studentCourseMapper.selectOne(wrapper);
    }

    @Override
    public boolean addOrUpdateScore(StudentCourse studentCourse) {
        StudentCourse existing = getStudentCourse(studentCourse.getUserId(), studentCourse.getCourseId());

        // 计算绩点（60分=1.0，每增加1分加0.1，最高4.0）
        if (studentCourse.getScore() != null) {
            BigDecimal score = studentCourse.getScore();
            BigDecimal gp;
            if (score.compareTo(BigDecimal.valueOf(60)) < 0) {
                gp = BigDecimal.ZERO;
                studentCourse.setIsPassed(0);
                studentCourse.setStatus("failed");
            } else {
                gp = BigDecimal.valueOf(1.0)
                        .add(score.subtract(BigDecimal.valueOf(60))
                                .divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_UP));
                if (gp.compareTo(BigDecimal.valueOf(4.0)) > 0) {
                    gp = BigDecimal.valueOf(4.0);
                }
                studentCourse.setIsPassed(1);
                studentCourse.setStatus("completed");
            }
            studentCourse.setGradePoint(gp);
        }

        // 设置默认值
        if (studentCourse.getSemester() == null) {
            studentCourse.setSemester("2026-2027-1"); // 默认学期
        }
        if (studentCourse.getIsRetake() == null) {
            studentCourse.setIsRetake(0);
        }
        if (studentCourse.getRetakeCount() == null) {
            studentCourse.setRetakeCount(0);
        }

        if (existing != null) {
            studentCourse.setId(existing.getId());
            studentCourseMapper.updateById(studentCourse);
        } else {
            studentCourseMapper.insert(studentCourse);
        }

        // 更新毕业进度
        calculateAndUpdateGraduationProgress(studentCourse.getUserId(), studentCourse.getMajorId());

        return true;
    }

    @Override
    public boolean updateStatus(Long id, String status, BigDecimal score, BigDecimal gradePoint) {
        StudentCourse sc = new StudentCourse();
        sc.setId(id);
        sc.setStatus(status);
        if (score != null) {
            sc.setScore(score);
        }
        if (gradePoint != null) {
            sc.setGradePoint(gradePoint);
        }
        return studentCourseMapper.updateById(sc) > 0;
    }

    @Override
    public GraduationProgressVO getGraduationProgress(Long userId, Long majorId) {
        Major major = majorService.getMajorById(majorId);
        GraduationRequirement requirement = graduationRequirementService.getByUserIdAndMajorId(userId, majorId);

        GraduationProgressVO vo = new GraduationProgressVO();

        if (major != null) {
            vo.setMajorName(major.getName());
            vo.setMajorCode(major.getCode());
            vo.setTotalCreditsRequired(major.getTotalCredits());
            vo.setCompulsoryCreditsRequired(major.getCompulsoryCredits());
            vo.setElectiveCreditsRequired(major.getElectiveCredits());
        }

        if (requirement != null) {
            vo.setTotalCreditsEarned(requirement.getTotalCreditsEarned());
            vo.setCompulsoryCreditsEarned(requirement.getCompulsoryCreditsEarned());
            vo.setElectiveCreditsEarned(requirement.getElectiveCreditsEarned());
            vo.setGpa(requirement.getGpa());
            vo.setProgressPercent(requirement.getProgressPercent());
            vo.setStatus(requirement.getStatus());
        } else {
            vo.setTotalCreditsEarned(0);
            vo.setCompulsoryCreditsEarned(0);
            vo.setElectiveCreditsEarned(0);
            vo.setGpa(BigDecimal.ZERO);
            vo.setProgressPercent(0);
            vo.setStatus("studying");
        }

        return vo;
    }

    @Override
    public void calculateAndUpdateGraduationProgress(Long userId, Long majorId) {
        // 获取学生所有已通过的课程
        List<StudentCourse> passedCourses = studentCourseMapper.selectList(
                new LambdaQueryWrapper<StudentCourse>()
                        .eq(StudentCourse::getUserId, userId)
                        .eq(StudentCourse::getMajorId, majorId)
                        .eq(StudentCourse::getIsPassed, 1)
        );

        if (passedCourses.isEmpty()) {
            return;
        }

        // 获取课程信息
        List<Long> courseIds = passedCourses.stream()
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toList());

        List<Course> courses = courseService.listByIds(courseIds);
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        // 计算已修学分
        int totalEarned = 0;
        int compulsoryEarned = 0;
        int electiveEarned = 0;

        for (StudentCourse sc : passedCourses) {
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
        for (StudentCourse sc : passedCourses) {
            if (sc.getGradePoint() != null && sc.getGradePoint().compareTo(BigDecimal.ZERO) > 0) {
                totalGp = totalGp.add(sc.getGradePoint());
                validCount++;
            }
        }
        BigDecimal avgGpa = validCount > 0 ? totalGp.divide(BigDecimal.valueOf(validCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 获取专业要求
        Major major = majorService.getMajorById(majorId);
        if (major == null) return;

        // 计算进度百分比
        int progressPercent = (int) ((double) totalEarned / major.getTotalCredits() * 100);
        if (progressPercent > 100) progressPercent = 100;

        // 更新或创建毕业要求记录
        GraduationRequirement requirement = graduationRequirementService.getByUserIdAndMajorId(userId, majorId);
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

        if (totalEarned >= major.getTotalCredits()) {
            requirement.setStatus("completed");
        } else {
            requirement.setStatus("studying");
        }

        graduationRequirementService.saveOrUpdate(requirement);
    }

    @Override
    public GraduationGapVO getGraduationGap(Long userId, Long majorId) {
        GraduationGapVO gap = new GraduationGapVO();
        gap.setProgress(getGraduationProgress(userId, majorId));

        List<Course> allCourses = courseService.getCoursesByMajorId(majorId);
        List<StudentCourse> studentCourses = getStudentCoursesByMajor(userId, majorId);
        Map<Long, StudentCourse> scMap = studentCourses.stream()
                .collect(Collectors.toMap(StudentCourse::getCourseId, sc -> sc, (a, b) -> a));

        Set<String> passedNames = new HashSet<>();
        for (StudentCourse sc : studentCourses) {
            if (sc.getIsPassed() != null && sc.getIsPassed() == 1) {
                Course c = courseService.getCourseById(sc.getCourseId());
                if (c != null) {
                    passedNames.add(c.getName());
                    if (c.getCourseCode() != null) {
                        passedNames.add(c.getCourseCode());
                    }
                }
            }
        }

        for (Course course : allCourses) {
            StudentCourse sc = scMap.get(course.getId());
            boolean passed = sc != null && sc.getIsPassed() != null && sc.getIsPassed() == 1;
            if (!passed) {
                MissingCourseVO missing = new MissingCourseVO();
                missing.setCourseId(course.getId());
                missing.setCourseCode(course.getCourseCode());
                missing.setName(course.getName());
                missing.setCredit(course.getCredit());
                missing.setCourseType(course.getCourseType());
                missing.setTerm(course.getTerm());
                missing.setReason("未修读或未通过");
                if ("compulsory".equals(course.getCourseType())) {
                    gap.getMissingCompulsory().add(missing);
                } else {
                    gap.getMissingElective().add(missing);
                }
            }
            if (course.getPrerequisite() != null && !course.getPrerequisite().isBlank() && !passed) {
                String prereq = course.getPrerequisite().trim();
                boolean met = passedNames.stream().anyMatch(n -> prereq.contains(n) || n.contains(prereq));
                if (!met) {
                    PrerequisiteGapVO pg = new PrerequisiteGapVO();
                    pg.setCourseId(course.getId());
                    pg.setCourseName(course.getName());
                    pg.setPrerequisite(prereq);
                    pg.setMissingPrerequisiteCourse(prereq);
                    gap.getPrerequisiteGaps().add(pg);
                }
            }
        }

        GraduationProgressVO p = gap.getProgress();
        if (p != null) {
            gap.setCompulsoryCreditsShort(Math.max(0,
                    safe(p.getCompulsoryCreditsRequired()) - safe(p.getCompulsoryCreditsEarned())));
            gap.setElectiveCreditsShort(Math.max(0,
                    safe(p.getElectiveCreditsRequired()) - safe(p.getElectiveCreditsEarned())));
            gap.setTotalCreditsShort(Math.max(0,
                    safe(p.getTotalCreditsRequired()) - safe(p.getTotalCreditsEarned())));
        }
        return gap;
    }

    @Override
    public List<SemesterScheduleVO> getSemesterSchedule(Long userId, Long majorId) {
        List<Course> courses = courseService.getCoursesByMajorId(majorId);
        List<StudentCourse> studentCourses = getStudentCoursesByMajor(userId, majorId);
        Map<Long, StudentCourse> scMap = studentCourses.stream()
                .collect(Collectors.toMap(StudentCourse::getCourseId, sc -> sc, (a, b) -> a));

        Map<Integer, List<Course>> byTerm = courses.stream()
                .collect(Collectors.groupingBy(c -> c.getTerm() != null ? c.getTerm() : 0));

        List<SemesterScheduleVO> result = new ArrayList<>();
        byTerm.keySet().stream().sorted().forEach(term -> {
            SemesterScheduleVO semester = new SemesterScheduleVO();
            semester.setTerm(term);
            semester.setTermLabel(term > 0 ? "第" + term + "学期" : "未分学期");
            int credits = 0;
            for (Course course : byTerm.get(term)) {
                ScheduleCourseVO item = new ScheduleCourseVO();
                item.setCourseId(course.getId());
                item.setCourseCode(course.getCourseCode());
                item.setName(course.getName());
                item.setCredit(course.getCredit());
                item.setCourseType(course.getCourseType());
                item.setExamType(course.getExamType());
                StudentCourse sc = scMap.get(course.getId());
                if (sc != null) {
                    item.setStudentStatus(sc.getStatus());
                    item.setScore(sc.getScore());
                    item.setGradePoint(sc.getGradePoint());
                    item.setIsPassed(sc.getIsPassed());
                } else {
                    item.setStudentStatus("pending");
                    item.setIsPassed(0);
                }
                if (course.getCredit() != null) {
                    credits += course.getCredit().intValue();
                }
                semester.getCourses().add(item);
            }
            semester.setTotalCredits(credits);
            result.add(semester);
        });
        return result;
    }

    @Override
    public GpaSimulateVO simulateGpa(Long userId, GpaSimulateRequest request) {
        Long majorId = request.getMajorId();
        GpaSimulateVO vo = new GpaSimulateVO();
        GraduationProgressVO progress = getGraduationProgress(userId, majorId);
        vo.setCurrentGpa(progress.getGpa() != null ? progress.getGpa() : BigDecimal.ZERO);
        vo.setScholarshipLine(BigDecimal.valueOf(3.5));
        vo.setCountedCourses(0);

        Map<Long, BigDecimal> overrideMap = new HashMap<>();
        if (request.getScores() != null) {
            for (GpaSimulateRequest.ScoreItem item : request.getScores()) {
                if (item.getCourseId() != null && item.getScore() != null) {
                    overrideMap.put(item.getCourseId(), item.getScore());
                }
            }
        }

        List<StudentCourse> studentCourses = getStudentCoursesByMajor(userId, majorId);
        Set<Long> processed = new HashSet<>();
        BigDecimal totalGp = BigDecimal.ZERO;
        int count = 0;

        for (StudentCourse sc : studentCourses) {
            BigDecimal score = overrideMap.getOrDefault(sc.getCourseId(), sc.getScore());
            if (score == null) continue;
            BigDecimal gp = calcGradePoint(score);
            if (gp.compareTo(BigDecimal.ZERO) > 0) {
                totalGp = totalGp.add(gp);
                count++;
            }
            processed.add(sc.getCourseId());
        }
        for (Map.Entry<Long, BigDecimal> e : overrideMap.entrySet()) {
            if (!processed.contains(e.getKey())) {
                BigDecimal gp = calcGradePoint(e.getValue());
                if (gp.compareTo(BigDecimal.ZERO) > 0) {
                    totalGp = totalGp.add(gp);
                    count++;
                }
            }
        }

        vo.setCountedCourses(count);
        BigDecimal projected = count > 0
                ? totalGp.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : vo.getCurrentGpa();
        vo.setProjectedGpa(projected);
        vo.setMeetsScholarship(projected.compareTo(vo.getScholarshipLine()) >= 0);
        vo.setScholarshipTip(vo.getMeetsScholarship()
                ? "预计可达奖学金线（≥3.5）"
                : "距奖学金线还差 " + vo.getScholarshipLine().subtract(projected).setScale(2, RoundingMode.HALF_UP) + " 分");
        return vo;
    }

    private int safe(Integer v) {
        return v != null ? v : 0;
    }

    private BigDecimal calcGradePoint(BigDecimal score) {
        if (score == null || score.compareTo(BigDecimal.valueOf(60)) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal gp = BigDecimal.valueOf(1.0)
                .add(score.subtract(BigDecimal.valueOf(60))
                        .divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_UP));
        return gp.compareTo(BigDecimal.valueOf(4.0)) > 0 ? BigDecimal.valueOf(4.0) : gp;
    }
}