package org.example.universityservice.mapper.major;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.universityservice.entity.major.GraduationRequirement;

@Mapper
@DS("futurestack")
public interface GraduationRequirementMapper extends BaseMapper<GraduationRequirement> {

    @Select("SELECT * FROM graduation_requirement WHERE user_id = #{userId} AND major_id = #{majorId}")
    GraduationRequirement selectByUserIdAndMajorId(@Param("userId") Long userId, @Param("majorId") Long majorId);

    @Update("UPDATE graduation_requirement SET progress_percent = #{percent}, updated_at = NOW() WHERE user_id = #{userId} AND major_id = #{majorId}")
    void updateProgress(@Param("userId") Long userId, @Param("majorId") Long majorId, @Param("percent") Integer percent);
}