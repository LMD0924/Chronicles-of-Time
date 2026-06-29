package org.example.universityservice.mapper.major;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.universityservice.entity.major.CourseCategory;

@Mapper
@DS("futurestack")
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {
}