/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.mapper.major;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.universityservice.entity.major.Course;

import java.util.List;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
@DS("cot_university")
public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT * FROM uni_course WHERE major_id = #{majorId} AND status = 1 ORDER BY term_no, sort_order")
    List<Course> selectByMajorId(@Param("majorId") Long majorId);

    @Select("SELECT * FROM uni_course WHERE major_id = #{majorId} AND term_no = #{term} AND status = 1 ORDER BY sort_order")
    List<Course> selectByMajorIdAndTerm(@Param("majorId") Long majorId, @Param("term") Integer term);

    @Select("SELECT * FROM uni_course WHERE major_id = #{majorId} AND course_type = #{courseType} AND status = 1")
    List<Course> selectByMajorIdAndType(@Param("majorId") Long majorId, @Param("courseType") String courseType);
}