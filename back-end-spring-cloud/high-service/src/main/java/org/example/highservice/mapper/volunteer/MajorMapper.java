package org.example.highservice.mapper.volunteer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.highservice.entity.volunteer.Major;
import java.util.List;

@Mapper
public interface MajorMapper extends BaseMapper<Major> {
    List<Major> searchMajors(@Param("keyword") String keyword,
                             @Param("category") String category,
                             @Param("universityId") Integer universityId);
}