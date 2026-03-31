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
