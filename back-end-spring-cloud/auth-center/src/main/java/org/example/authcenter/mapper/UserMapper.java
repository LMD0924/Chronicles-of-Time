package org.example.authcenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.authcenter.entity.User;
import org.example.authcenter.vo.UserVO;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}