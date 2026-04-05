package org.example.highservice.mapper.volunteer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.highservice.entity.volunteer.UserVolunteer;
import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 用户志愿表Mapper
 */
@Mapper
public interface UserVolunteerMapper extends BaseMapper<UserVolunteer> {

    /**
     * 获取用户历年志愿方案
     */
    List<UserVolunteer> getUserVolunteerHistory(@Param("userId") Integer userId);

    /**
     * 获取志愿填报统计
     */
    List<Map<String, Object>> getVolunteerStatistics(@Param("userId") Integer userId);
}