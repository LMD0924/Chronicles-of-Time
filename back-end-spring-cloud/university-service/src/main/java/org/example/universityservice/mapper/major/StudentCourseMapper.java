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

@Mapper
@DS("futurestack")
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {

    @Select("SELECT * FROM student_course WHERE user_id = #{userId}")
    List<StudentCourse> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM student_course WHERE user_id = #{userId} AND major_id = #{majorId}")
    List<StudentCourse> selectByUserIdAndMajorId(@Param("userId") Long userId, @Param("majorId") Long majorId);

    @Select("SELECT SUM(credit) FROM student_course sc JOIN course c ON sc.course_id = c.id WHERE sc.user_id = #{userId} AND sc.is_passed = 1")
    BigDecimal getTotalEarnedCredits(@Param("userId") Long userId);

    @Select("SELECT AVG(grade_point) FROM student_course WHERE user_id = #{userId} AND grade_point IS NOT NULL AND grade_point > 0")
    BigDecimal getAverageGpa(@Param("userId") Long userId);

    @Update("UPDATE student_course SET status = 'completed', is_passed = 1, updated_at = NOW() WHERE user_id = #{userId} AND course_id = #{courseId}")
    void updateToCompleted(@Param("userId") Long userId, @Param("courseId") Long courseId);
}