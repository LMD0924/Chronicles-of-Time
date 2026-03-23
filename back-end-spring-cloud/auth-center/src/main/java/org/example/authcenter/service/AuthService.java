package org.example.authcenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.authcenter.dto.AuthDTO;
import org.example.authcenter.entity.User;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
public interface AuthService extends IService<User> {

    /**
     * 用户登录
     */
    String login(AuthDTO authDTO);

    /**
     * 用户注册
     */
    Integer register(AuthDTO authDTO);
}
