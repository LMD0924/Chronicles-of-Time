package org.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.usercenter.entity.UserInfo;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2026/4/5
 * @Description: 用户信息服务接口
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 新增用户信息
     */
    boolean addUserInfo(UserInfo userInfo);

    /**
     * 根据ID修改用户信息
     */
    boolean updateUserInfo(UserInfo userInfo);

    /**
     * 根据ID查询用户信息
     */
    UserInfo getUserInfoById(Long id);

    /**
     * 根据用户ID查询信息
     */
    UserInfo getUserInfoByUserId(Long userId);

    /**
     * 查询所有用户信息
     */
    List<UserInfo> getAllUserInfo();

    /**
     * 根据ID删除用户信息
     */
    boolean deleteUserInfoById(Long id);
}