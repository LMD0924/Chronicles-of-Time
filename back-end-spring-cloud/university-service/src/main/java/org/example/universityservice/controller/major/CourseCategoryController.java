package org.example.universityservice.controller.major;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.universityservice.entity.major.CourseCategory;
import org.example.universityservice.service.major.CourseCategoryService;
import org.example.commondb.utils.RestBean;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/course-category")
@RequiredArgsConstructor
public class CourseCategoryController {

    private final CourseCategoryService categoryService;

    @GetMapping("/list")
    public RestBean<List<CourseCategory>> getCategories(@RequestParam Long majorId, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
        
        List<CourseCategory> categories = userId != null ? categoryService.getCategoriesByMajorIdAndUserId(majorId, userId) : categoryService.getCategoriesByMajorId(majorId);
        return RestBean.success(categories);
    }

    @GetMapping("/root")
    public RestBean<List<CourseCategory>> getRootCategories(@RequestParam Long majorId, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
        
        List<CourseCategory> categories = userId != null ? categoryService.getRootCategoriesByUserId(majorId, userId) : categoryService.getRootCategories(majorId);
        return RestBean.success(categories);
    }

    @GetMapping("/{id}")
    public RestBean<CourseCategory> getCategoryById(@PathVariable Long id) {
        CourseCategory category = categoryService.getCategoryById(id);
        return category != null ? RestBean.success(category) : RestBean.fail("类别不存在");
    }

    @PostMapping("/create")
    public RestBean<String> createCategory(@RequestBody CourseCategory category) {
        boolean success = categoryService.createCategory(category);
        return success ? RestBean.success("创建成功") : RestBean.fail("创建失败");
    }

    @PutMapping("/update")
    public RestBean<String> updateCategory(@RequestBody CourseCategory category) {
        if (category.getId() == null) {
            return RestBean.fail("类别ID不能为空");
        }
        boolean success = categoryService.updateCategory(category);
        return success ? RestBean.success("更新成功") : RestBean.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public RestBean<String> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
        
        boolean success = categoryService.deleteCategory(id, userId);
        return success ? RestBean.success("删除成功") : RestBean.fail("删除失败");
    }
}