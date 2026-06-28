package org.example.universityservice.controller.major;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.universityservice.entity.major.Course;
import org.example.universityservice.service.major.CourseService;
import org.example.universityservice.vo.major.CourseTreeVO;
import org.example.universityservice.vo.major.CourseVO;
import org.example.commondb.utils.RestBean;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/list")
    public RestBean<List<CourseVO>> getCourses(@RequestParam Long majorId, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
        
        List<Course> courses = userId != null ? courseService.getCoursesByMajorIdAndUserId(majorId, userId) : courseService.getCoursesByMajorId(majorId);
        List<CourseVO> voList = courses.stream().map(c -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
        return RestBean.success(voList);
    }

    @GetMapping("/tree")
    public RestBean<CourseTreeVO> getCourseTree(@RequestParam Long majorId, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;

        CourseTreeVO tree = courseService.getCourseTree(majorId, userId);
        return RestBean.success(tree);
    }

    @GetMapping("/by-term")
    public RestBean<List<CourseVO>> getCoursesByTerm(@RequestParam Long majorId, @RequestParam Integer term) {
        List<Course> courses = courseService.getCoursesByMajorIdAndTerm(majorId, term);
        List<CourseVO> voList = courses.stream().map(c -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
        return RestBean.success(voList);
    }

    @PostMapping("/create")
    public RestBean<String> createCourse(@RequestBody CourseVO courseVO) {
        Course course = new Course();
        BeanUtils.copyProperties(courseVO, course);
        boolean success = courseService.createCourse(course);
        return success ? RestBean.success("创建成功") : RestBean.fail("创建失败");
    }

    @PutMapping("/update")
    public RestBean<String> updateCourse(@RequestBody CourseVO courseVO) {
        if (courseVO.getId() == null) {
            return RestBean.fail("课程ID不能为空");
        }
        Course course = new Course();
        BeanUtils.copyProperties(courseVO, course);
        boolean success = courseService.updateCourse(course);
        return success ? RestBean.success("更新成功") : RestBean.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public RestBean<String> deleteCourse(@PathVariable Long id, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
        
        boolean success = courseService.deleteCourse(id, userId);
        return success ? RestBean.success("删除成功") : RestBean.fail("删除失败");
    }

    @GetMapping("/{id}")
    public RestBean<CourseVO> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return RestBean.fail("课程不存在");
        }
        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);
        return RestBean.success(vo);
    }

    @GetMapping("/search")
    public RestBean<List<CourseVO>> searchCourses(@RequestParam Long majorId, @RequestParam String keyword, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
        
        List<Course> courses = userId != null ? courseService.searchCoursesByUserId(majorId, keyword, userId) : courseService.searchCourses(majorId, keyword);
        List<CourseVO> voList = courses.stream().map(c -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
        return RestBean.success(voList);
    }
}