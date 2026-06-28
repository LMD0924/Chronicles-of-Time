package org.example.universityservice.mapper.major;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.universityservice.entity.major.Course;

import java.util.List;

@Mapper
@DS("futurestack")
public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT * FROM course WHERE major_id = #{majorId} AND status = 1 ORDER BY term, sort_order")
    List<Course> selectByMajorId(@Param("majorId") Long majorId);

    @Select("SELECT * FROM course WHERE major_id = #{majorId} AND term = #{term} AND status = 1 ORDER BY sort_order")
    List<Course> selectByMajorIdAndTerm(@Param("majorId") Long majorId, @Param("term") Integer term);

    @Select("SELECT * FROM course WHERE major_id = #{majorId} AND course_type = #{courseType} AND status = 1")
    List<Course> selectByMajorIdAndType(@Param("majorId") Long majorId, @Param("courseType") String courseType);
}