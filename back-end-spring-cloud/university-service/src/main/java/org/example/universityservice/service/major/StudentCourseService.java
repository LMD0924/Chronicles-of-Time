/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

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