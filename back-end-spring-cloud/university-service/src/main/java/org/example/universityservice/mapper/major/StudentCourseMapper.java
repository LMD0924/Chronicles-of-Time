/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.mapper.major;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.universityservice.entity.major.StudentCourse;

import java.math.BigDecimal;
import java.util.List;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
@DS("cot_university")
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {

    @Select("SELECT * FROM uni_student_course WHERE user_id = #{userId}")
    List<StudentCourse> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM uni_student_course WHERE user_id = #{userId} AND major_id = #{majorId}")
    List<StudentCourse> selectByUserIdAndMajorId(@Param("userId") Long userId, @Param("majorId") Long majorId);

    @Select("SELECT SUM(credit) FROM uni_student_course sc JOIN uni_course c ON sc.course_id = c.id WHERE sc.user_id = #{userId} AND sc.is_passed = 1")
    BigDecimal getTotalEarnedCredits(@Param("userId") Long userId);

    @Select("SELECT AVG(grade_point) FROM uni_student_course WHERE user_id = #{userId} AND grade_point IS NOT NULL AND grade_point > 0")
    BigDecimal getAverageGpa(@Param("userId") Long userId);

    @Update("UPDATE uni_student_course SET status = 'completed', is_passed = 1, updated_at = NOW() WHERE user_id = #{userId} AND course_id = #{courseId}")
    void updateToCompleted(@Param("userId") Long userId, @Param("courseId") Long courseId);
}