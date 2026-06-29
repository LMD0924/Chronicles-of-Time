package org.example.universityservice.service.major;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.major.CourseCategory;

import java.util.List;

public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategory> getCategoriesByMajorId(Long majorId);

    List<CourseCategory> getCategoriesByMajorIdAndUserId(Long majorId, Long userId);

    List<CourseCategory> getRootCategories(Long majorId);

    List<CourseCategory> getRootCategoriesByUserId(Long majorId, Long userId);

    List<CourseCategory> getChildCategories(Long parentId);

    CourseCategory getCategoryById(Long id);

    boolean createCategory(CourseCategory category);

    boolean updateCategory(CourseCategory category);

    boolean deleteCategory(Long id, Long userId);
}