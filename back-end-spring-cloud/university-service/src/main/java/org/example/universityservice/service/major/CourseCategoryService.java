/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.service.major;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.major.CourseCategory;

import java.util.List;
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

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