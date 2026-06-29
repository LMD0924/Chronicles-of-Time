package org.example.universityservice.service.major;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.major.StudentCourse;
import org.example.universityservice.vo.major.GpaSimulateRequest;
import org.example.universityservice.vo.major.GpaSimulateVO;
import org.example.universityservice.vo.major.GraduationGapVO;
import org.example.universityservice.vo.major.GraduationProgressVO;
import org.example.universityservice.vo.major.SemesterScheduleVO;

import java.math.BigDecimal;
import java.util.List;

public interface StudentCourseService extends IService<StudentCourse> {

    List<StudentCourse> getStudentCourses(Long userId);

    List<StudentCourse> getStudentCoursesByMajor(Long userId, Long majorId);

    StudentCourse getStudentCourse(Long userId, Long courseId);

    boolean addOrUpdateScore(StudentCourse studentCourse);

    boolean updateStatus(Long id, String status, BigDecimal score, BigDecimal gradePoint);

    GraduationProgressVO getGraduationProgress(Long userId, Long majorId);

    void calculateAndUpdateGraduationProgress(Long userId, Long majorId);

    GraduationGapVO getGraduationGap(Long userId, Long majorId);

    List<SemesterScheduleVO> getSemesterSchedule(Long userId, Long majorId);

    GpaSimulateVO simulateGpa(Long userId, GpaSimulateRequest request);
}