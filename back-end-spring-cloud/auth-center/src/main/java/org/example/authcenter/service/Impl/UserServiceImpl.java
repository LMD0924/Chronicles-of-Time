/**
 * 文件说明：拾光记微服务后端认证中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.authcenter.entity.User;
import org.example.authcenter.mapper.UserMapper;
import org.example.authcenter.service.UserService;
import org.example.authcenter.vo.UserVO;
import org.springframework.stereotype.Service;

/*
 * @Author:总会落叶
 * @Date:2026/3/24
 * @Description:
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
      private final UserMapper userMapper;

    /**
     * 根据id获取用户信息
     */
    @Override
    public User getUserById(Long id){
        return userMapper.selectById(id);
    }

    /**
     * 上传头像
     */
    @Override
    public boolean uploadAvatar(Long userId,String avatarUrl){
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        return userMapper.updateById(user) > 0;
    }

    /**
     * 修改用户信息
     */
    @Override
    public boolean updateUserInfo(User user){
        if(user.getId()==null) return false;
        return userMapper.updateById(user) > 0;
    }
}
