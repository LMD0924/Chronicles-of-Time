package org.example.authcenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.authcenter.dto.AuthDTO;
import org.example.authcenter.entity.User;
import org.example.authcenter.vo.LoginVO;
import org.example.authcenter.vo.UserVO;
import org.example.commondb.utils.RestBean;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
public interface AuthService extends IService<User> {

    /**
     * 用户登录
     */
    LoginVO login(AuthDTO authDTO);

    /**
     * 用户注册
     */
    Integer register(AuthDTO authDTO);

    /**
     * 刷新Token
     * @param refreshToken 刷新令牌
     * @return 新的登录信息
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 用户登出
     * @param accessToken 访问令牌
     * @param userId 用户ID
     */
    void logout(String accessToken, Long userId);

    /**
     * 验证Token（供网关调用）
     * @param token 令牌
     * @return 验证结果
     */
    RestBean<Object> verifyToken(String token);
}
