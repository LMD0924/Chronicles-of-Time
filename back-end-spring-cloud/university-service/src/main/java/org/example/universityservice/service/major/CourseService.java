/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.service.major;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.major.Course;
import org.example.universityservice.vo.major.CourseTreeVO;

import java.util.List;
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

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