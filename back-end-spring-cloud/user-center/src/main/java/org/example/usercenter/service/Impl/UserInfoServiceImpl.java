/**
 * 文件说明：拾光记微服务后端用户中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.usercenter.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.usercenter.entity.UserInfo;
import org.example.usercenter.mapper.UserInfoMapper;
import org.example.usercenter.service.UserInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2026/4/5
 * @Description: 用户信息服务实现类
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Override
    public boolean addUserInfo(UserInfo userInfo) {
        // 新增前可添加业务校验，如检查是否已存在
        return save(userInfo);
    }

    @Override
    public boolean updateUserInfo(UserInfo userInfo) {
        // 检查ID是否存在
        if (userInfo.getId() == null) {
            return false;
        }
        return updateById(userInfo);
    }

    @Override
    public UserInfo getUserInfoById(Long id) {
        return getById(id);
    }

    @Override
    public UserInfo getUserInfoByUserId(Long userId) {
        // 根据用户ID查询
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserId, userId);
        return getOne(wrapper);
    }

    @Override
    public List<UserInfo> getAllUserInfo() {
        return list();
    }

    @Override
    public boolean deleteUserInfoById(Long id) {
        return removeById(id);
    }
}