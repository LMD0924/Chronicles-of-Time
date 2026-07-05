package org.example.generalservice.mapper.chat;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.generalservice.entity.chat.ChatGroup;

@Mapper
@DS("cot_content")
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {
}
