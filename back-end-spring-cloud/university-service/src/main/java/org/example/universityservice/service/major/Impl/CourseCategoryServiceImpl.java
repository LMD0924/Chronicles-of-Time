/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.service.major.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.universityservice.entity.major.CourseCategory;
import org.example.universityservice.mapper.major.CourseCategoryMapper;
import org.example.universityservice.service.major.CourseCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    private final CourseCategoryMapper categoryMapper;

    @Override
    public List<CourseCategory> getCategoriesByMajorId(Long majorId) {
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCategory::getMajorId, majorId)
                .orderByAsc(CourseCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<CourseCategory> getCategoriesByMajorIdAndUserId(Long majorId, Long userId) {
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCategory::getMajorId, majorId)
                .and(w -> w.isNull(CourseCategory::getUserId).or().eq(CourseCategory::getUserId, userId))
                .orderByAsc(CourseCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<CourseCategory> getRootCategories(Long majorId) {
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCategory::getMajorId, majorId)
                .eq(CourseCategory::getParentId, 0)
                .orderByAsc(CourseCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<CourseCategory> getRootCategoriesByUserId(Long majorId, Long userId) {
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCategory::getMajorId, majorId)
                .eq(CourseCategory::getParentId, 0)
                .and(w -> w.isNull(CourseCategory::getUserId).or().eq(CourseCategory::getUserId, userId))
                .orderByAsc(CourseCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<CourseCategory> getChildCategories(Long parentId) {
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCategory::getParentId, parentId)
                .orderByAsc(CourseCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public CourseCategory getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public boolean createCategory(CourseCategory category) {
        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean updateCategory(CourseCategory category) {
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public boolean deleteCategory(Long id, Long userId) {
        // 先删除子节点
        LambdaQueryWrapper<CourseCategory> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(CourseCategory::getParentId, id);
        categoryMapper.delete(childWrapper);
        // 再删除自己（只允许删除自己创建的或无用户ID的分类）
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCategory::getId, id)
                .and(w -> w.isNull(CourseCategory::getUserId).or().eq(CourseCategory::getUserId, userId));
        return categoryMapper.delete(wrapper) > 0;
    }
}