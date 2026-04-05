package org.example.highservice.mapper.volunteer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.highservice.entity.volunteer.University;
import java.util.List;

@Mapper
public interface UniversityMapper extends BaseMapper<University> {
    List<University> searchUniversities(@Param("keyword") String keyword,
                                        @Param("province") String province,
                                        @Param("level") String level);
}