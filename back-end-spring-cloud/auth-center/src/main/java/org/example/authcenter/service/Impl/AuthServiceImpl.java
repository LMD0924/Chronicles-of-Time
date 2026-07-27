/**
 * 文件说明：拾光记微服务后端认证中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.authcenter.dto.AuthDTO;
import org.example.authcenter.entity.User;
import org.example.authcenter.mapper.AuthMapper;
import org.example.authcenter.service.AuthService;
import org.example.authcenter.vo.LoginVO;
import org.example.authcenter.vo.UserVO;
import org.example.commoncore.auth.RoleCodes;
import org.example.commoncore.utils.JwtUtil;
import org.example.commondb.utils.RestBean;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<AuthMapper, User> implements AuthService {

    private final AuthMapper authMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(AuthDTO authDTO) {
        if (authDTO == null || StringUtils.isBlank(authDTO.getUsername()) || StringUtils.isBlank(authDTO.getPassword())) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        User user = lambdaQuery().eq(User::getUsername, authDTO.getUsername()).one();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        if (!passwordEncoder.matches(authDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return issueTokens(user, loadRoles(user.getId()), loadPermissions(user.getId()), authDTO.getRememberMe());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer register(AuthDTO authDTO) {
        if (authDTO == null || StringUtils.isBlank(authDTO.getUsername()) || StringUtils.isBlank(authDTO.getPassword())) {
            throw new RuntimeException("用户名或密码不能为空");
        }
        String username = authDTO.getUsername().trim();
        long count = lambdaQuery().eq(User::getUsername, username).count();
        if (count > 0) {
            throw new RuntimeException("用户已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(authDTO.getPassword()));
        user.setName(resolveDisplayName(authDTO, username));
        user.setEmail(StringUtils.isBlank(authDTO.getEmail()) ? null : authDTO.getEmail().trim());
        user.setPhone(StringUtils.isBlank(authDTO.getPhone()) ? null : authDTO.getPhone().trim());
        user.setUserType(1);
        user.setRegisterChannel("web");
        user.setStatus(1);
        int inserted = authMapper.insert(user);
        if (inserted > 0) {
            Long roleId = authMapper.selectRoleIdByCode(RoleCodes.USER);
            if (roleId != null) {
                authMapper.insertUserRole(user.getId(), user.getId(), roleId);
            }
        }
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String sessionId = jwtUtil.getTokenId(refreshToken);
        if (StringUtils.isBlank(sessionId) || authMapper.countActiveTokenSessions(userId, sessionId) == 0) {
            throw new RuntimeException("刷新令牌已失效，请重新登录");
        }

        User user = authMapper.selectById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        authMapper.revokeTokenSession(userId, sessionId);
        return issueTokens(user, loadRoles(userId), loadPermissions(userId), false);
    }

    @Override
    public void logout(String accessToken, Long userId) {
        if (!jwtUtil.validateToken(accessToken) || !jwtUtil.isAccessToken(accessToken)
                || !userId.equals(jwtUtil.getUserIdFromToken(accessToken))) {
            throw new RuntimeException("无效的访问令牌");
        }
        String sessionId = jwtUtil.getTokenId(accessToken);
        if (StringUtils.isBlank(sessionId)) {
            throw new RuntimeException("无效的访问令牌");
        }
        authMapper.revokeTokenSession(userId, sessionId);
        log.info("用户登出成功: userId={}", userId);
    }

    @Override
    public RestBean<Object> verifyToken(String token) {
        if (!jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
            return RestBean.fail(401, "Token无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String sessionId = jwtUtil.getTokenId(token);
        if (StringUtils.isBlank(sessionId) || authMapper.countActiveTokenSessions(userId, sessionId) == 0) {
            return RestBean.fail(401, "Token已失效");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        List<String> roles = jwtUtil.getRolesFromToken(token);
        List<String> permissions = jwtUtil.getPermissionsFromToken(token);
        return RestBean.success("验证通过", java.util.Map.of(
                "userId", userId,
                "username", username,
                "roles", roles,
                "permissions", permissions,
                "valid", true
        ));
    }

    private LoginVO issueTokens(User user, List<String> roles, List<String> permissions, Boolean rememberMe) {
        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateAccessTokenWithRememberMe(
                user.getUsername(), user.getId(), roles, permissions, rememberMe, sessionId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId(), sessionId);
        if (authMapper.insertTokenSession(IdWorker.getId(), user.getId(), sessionId,
                jwtUtil.getExpirationDateFromToken(refreshToken)) != 1) {
            throw new RuntimeException("登录会话创建失败");
        }

        Long expiration = jwtUtil.getRemainingTime(accessToken);
        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .roles(roles)
                .permissions(permissions)
                .userInfo(buildUserVO(user, roles, permissions))
                .build();
    }

    private List<String> loadRoles(Long userId) {
        List<String> roles = authMapper.selectRoleCodesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>(List.of(RoleCodes.USER));
        }
        return roles;
    }

    private List<String> loadPermissions(Long userId) {
        List<String> permissions = authMapper.selectPermissionCodesByUserId(userId);
        return permissions == null ? List.of() : permissions;
    }

    private UserVO buildUserVO(User user, List<String> roles, List<String> permissions) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRoles(roles);
        userVO.setPermissions(permissions);
        userVO.setRole(roles == null || roles.isEmpty() ? RoleCodes.USER : roles.get(0));
        return userVO;
    }

    private String resolveDisplayName(AuthDTO authDTO, String username) {
        if (StringUtils.isNotBlank(authDTO.getName())) {
            return authDTO.getName().trim();
        }
        if (StringUtils.isNotBlank(authDTO.getDisplayName())) {
            return authDTO.getDisplayName().trim();
        }
        return username;
    }
}
