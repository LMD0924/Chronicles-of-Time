/**
 * 文件说明：拾光记微服务后端认证中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.authcenter.entity.User;
import org.example.authcenter.mapper.UserMapper;
import org.example.authcenter.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

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

    @Override
    public List<User> searchPublicUsers(String keyword, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            wrapper.and(q -> q.like(User::getUsername, text)
                    .or()
                    .like(User::getName, text)
                    .or()
                    .like(User::getEmail, text)
                    .or()
                    .like(User::getPhone, text));
        }
        wrapper.orderByDesc(User::getLastLoginTime)
                .last("LIMIT " + safeLimit);
        return userMapper.selectList(wrapper);
    }
}
