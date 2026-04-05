package org.example.highservice.mapper.volunteer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.highservice.entity.volunteer.AdmissionPlan;
import java.util.List;

@Mapper
public interface AdmissionPlanMapper extends BaseMapper<AdmissionPlan> {
    List<AdmissionPlan> getAdmissionHistory(@Param("majorId") Integer majorId,
                                            @Param("universityId") Integer universityId,
                                            @Param("year") Integer year);

    AdmissionPlan getLatestAdmission(@Param("majorId") Integer majorId,
                                     @Param("universityId") Integer universityId);
}