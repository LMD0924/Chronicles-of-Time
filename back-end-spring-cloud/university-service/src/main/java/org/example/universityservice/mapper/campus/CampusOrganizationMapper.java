package org.example.universityservice.mapper.campus;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.universityservice.entity.campus.CampusOrganization;

@Mapper
@DS("cot_university")
public interface CampusOrganizationMapper extends BaseMapper<CampusOrganization> {
}
