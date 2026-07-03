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
import org.example.universityservice.entity.major.CourseCategory;
import org.example.universityservice.entity.major.StudentCourse;
import org.example.universityservice.mapper.major.CourseMapper;
import org.example.universityservice.service.major.CourseCategoryService;
import org.example.universityservice.service.major.CourseService;
import org.example.universityservice.service.major.StudentCourseService;
import org.example.universityservice.vo.major.CategoryTreeNodeVO;
import org.example.universityservice.vo.major.CourseTreeVO;
import org.example.universityservice.vo.major.CourseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseCategoryService categoryService;
    private StudentCourseService studentCourseService;
    // 构造函数注入非循环依赖的 Bean
    public CourseServiceImpl(CourseMapper courseMapper, CourseCategoryService categoryService) {
        this.courseMapper = courseMapper;
        this.categoryService = categoryService;
    }
    // Setter 注入循环依赖的 Bean，使用 @Lazy
    @Autowired
    @Lazy
    public void setStudentCourseService(StudentCourseService studentCourseService) {
        this.studentCourseService = studentCourseService;
    }

    @Override
    public List<Course> getCoursesByMajorId(Long majorId) {
        return courseMapper.selectByMajorId(majorId);
    }

    @Override
    public List<Course> getCoursesByMajorIdAndTerm(Long majorId, Integer term) {
        return courseMapper.selectByMajorIdAndTerm(majorId, term);
    }

    @Override
    public List<Course> getCoursesByMajorIdAndType(Long majorId, String courseType) {
        return courseMapper.selectByMajorIdAndType(majorId, courseType);
    }

    @Override
    public Course getCourseById(Long id) {
        return courseMapper.selectById(id);
    }

    @Override
    public CourseTreeVO getCourseTree(Long majorId, Long userId) {
        // 获取所有课程类别
        List<CourseCategory> categories = categoryService.getCategoriesByMajorId(majorId);

        // 获取所有课程
        List<Course> courses = getCoursesByMajorId(majorId);

        // 获取学生选课成绩
        List<StudentCourse> studentCourses = studentCourseService.getStudentCoursesByMajor(userId, majorId);

        // 构建课程ID到选课信息的映射
        Map<Long, StudentCourse> studentCourseMap = studentCourses.stream()
                .collect(Collectors.toMap(StudentCourse::getCourseId, sc -> sc, (v1, v2) -> v1));

        // 构建树形结构
        List<CategoryTreeNodeVO> treeNodes = new ArrayList<>();

        for (CourseCategory category : categories) {
            CategoryTreeNodeVO categoryNode = new CategoryTreeNodeVO();
            categoryNode.setId(category.getId());
            categoryNode.setName(category.getName());
            categoryNode.setIcon(category.getIcon());
            categoryNode.setColor(category.getColor());

            // 获取该类别下的课程
            List<CourseVO> courseVOs = new ArrayList<>();
            for (Course course : courses) {
                if (course.getCategoryId().equals(category.getId())) {
                    CourseVO courseVO = new CourseVO();
                    BeanUtils.copyProperties(course, courseVO);

                    // 填充学生选课信息
                    StudentCourse sc = studentCourseMap.get(course.getId());
                    if (sc != null) {
                        courseVO.setStudentStatus(sc.getStatus());
                        courseVO.setStudentScore(sc.getScore());
                        courseVO.setStudentGradePoint(sc.getGradePoint());
                        courseVO.setIsPassed(sc.getIsPassed());
                        courseVO.setIsRetake(sc.getIsRetake());
                    } else {
                        courseVO.setStudentStatus("planned");
                    }

                    courseVOs.add(courseVO);
                }
            }
            categoryNode.setCourses(courseVOs);
            treeNodes.add(categoryNode);
        }

        CourseTreeVO result = new CourseTreeVO();
        result.setCategories(treeNodes);
        result.setTotalCourses(courses.size());
        result.setCompulsoryCount((int) courses.stream().filter(c -> "compulsory".equals(c.getCourseType())).count());
        result.setElectiveCount((int) courses.stream().filter(c -> "elective".equals(c.getCourseType())).count());

        return result;
    }

    @Override
    public boolean createCourse(Course course) {
        course.setStatus(1);
        return courseMapper.insert(course) > 0;
    }

    @Override
    public boolean updateCourse(Course course) {
        return courseMapper.updateById(course) > 0;
    }

    @Override
    public List<Course> getCoursesByMajorIdAndUserId(Long majorId, Long userId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getMajorId, majorId)
                .eq(Course::getStatus, 1)
                .and(w -> w.isNull(Course::getUserId).or().eq(Course::getUserId, userId));
        return courseMapper.selectList(wrapper);
    }

    @Override
    public boolean deleteCourse(Long id, Long userId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getId, id)
                .and(w -> w.isNull(Course::getUserId).or().eq(Course::getUserId, userId));
        Course course = new Course();
        course.setStatus(0);
        return courseMapper.update(course, wrapper) > 0;
    }

    @Override
    public List<Course> searchCourses(Long majorId, String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getMajorId, majorId)
                .eq(Course::getStatus, 1)
                .and(w -> w.like(Course::getName, keyword)
                        .or()
                        .like(Course::getCourseCode, keyword));
        return courseMapper.selectList(wrapper);
    }

    @Override
    public List<Course> searchCoursesByUserId(Long majorId, String keyword, Long userId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getMajorId, majorId)
                .eq(Course::getStatus, 1)
                .and(w -> w.isNull(Course::getUserId).or().eq(Course::getUserId, userId))
                .and(w -> w.like(Course::getName, keyword)
                        .or()
                        .like(Course::getCourseCode, keyword));
        return courseMapper.selectList(wrapper);
    }
}