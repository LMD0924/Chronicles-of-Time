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
import org.example.universityservice.entity.major.GraduationRequirement;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
@DS("cot_university")
public interface GraduationRequirementMapper extends BaseMapper<GraduationRequirement> {

    @Select("SELECT * FROM uni_graduation_requirement WHERE user_id = #{userId} AND major_id = #{majorId}")
    GraduationRequirement selectByUserIdAndMajorId(@Param("userId") Long userId, @Param("majorId") Long majorId);

    @Update("UPDATE uni_graduation_requirement SET progress_percent = #{percent}, updated_at = NOW() WHERE user_id = #{userId} AND major_id = #{majorId}")
    void updateProgress(@Param("userId") Long userId, @Param("majorId") Long majorId, @Param("percent") Integer percent);
}