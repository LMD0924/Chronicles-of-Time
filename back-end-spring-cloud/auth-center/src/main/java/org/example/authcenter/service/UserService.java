/**
 * 文件说明：拾光记微服务后端认证中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.authcenter.entity.User;

import java.util.List;

/*
 * @Author:总会落叶
 * @Date:2026/3/24
 * @Description:
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
public interface UserService extends IService<User> {

    /**
     * 根据id获取用户信息
     */
    User getUserById(Long id);

    /**
     * 上传头像
     */
    boolean uploadAvatar(Long userId,String avatarUrl);

    /**
     * 修改用户信息
     */
    boolean updateUserInfo(User user);

    /**
     * 按账号、昵称、邮箱或手机号搜索启用用户。
     */
    List<User> searchPublicUsers(String keyword, int limit);
}
