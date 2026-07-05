package org.example.generalservice.mapper.activity;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.generalservice.entity.activity.MedalRule;

@Mapper
@DS("cot_content")
public interface MedalRuleMapper extends BaseMapper<MedalRule> {
}
