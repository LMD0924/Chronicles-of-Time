package org.example.generalservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.generalservice.entity.SystemNotification;

@Mapper
@DS("cot_platform")
public interface NotificationMapper extends BaseMapper<SystemNotification> {
}
