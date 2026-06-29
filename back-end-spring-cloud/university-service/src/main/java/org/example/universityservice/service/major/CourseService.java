package org.example.universityservice.service.major;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.major.Course;
import org.example.universityservice.vo.major.CourseTreeVO;

import java.util.List;

public interface CourseService extends IService<Course> {

    List<Course> getCoursesByMajorId(Long majorId);

    List<Course> getCoursesByMajorIdAndUserId(Long majorId, Long userId);

    List<Course> getCoursesByMajorIdAndTerm(Long majorId, Integer term);

    List<Course> getCoursesByMajorIdAndType(Long majorId, String courseType);

    Course getCourseById(Long id);

    CourseTreeVO getCourseTree(Long majorId, Long userId);

    boolean createCourse(Course course);

    boolean updateCourse(Course course);

    boolean deleteCourse(Long id, Long userId);

    List<Course> searchCourses(Long majorId, String keyword);

    List<Course> searchCoursesByUserId(Long majorId, String keyword, Long userId);
}