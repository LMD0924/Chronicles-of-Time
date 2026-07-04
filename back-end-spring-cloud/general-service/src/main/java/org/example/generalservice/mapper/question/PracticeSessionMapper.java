/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责练习和考试会话数据访问。
 */
package org.example.generalservice.mapper.question;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.generalservice.entity.PracticeSession;

/**
 * 练习或考试会话 Mapper。
 */
@Mapper
@DS("cot_learning")
public interface PracticeSessionMapper extends BaseMapper<PracticeSession> {
}
