package org.example.universityservice.mapper.paper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.universityservice.entity.paper.Suggestion;


@Mapper
@DS("futurestack")
public interface SuggestionMapper extends BaseMapper<Suggestion> {
}