/**
 * 文件说明：拾光记微服务后端认证中心业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.authcenter.dto.AdminUserDTO;
import org.example.authcenter.entity.User;
import org.example.authcenter.mapper.AuthMapper;
import org.example.authcenter.mapper.UserMapper;
import org.example.authcenter.service.UserService;
import org.example.authcenter.vo.PageVO;
import org.example.authcenter.vo.UserVO;
import org.example.commoncore.auth.RoleCodes;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

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
        if (org.springframework.util.StringUtils.hasText(keyword)) {
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

    @Override
    public PageVO<UserVO> pageAdminUsers(String keyword, Integer status, Integer userType, long page, long pageSize) {
        long safePage = Math.max(1, page);
        long safeSize = Math.max(1, Math.min(pageSize, 100));
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            String text = keyword.trim();
            wrapper.and(q -> q.like(User::getUsername, text)
                    .or()
                    .like(User::getName, text)
                    .or()
                    .like(User::getEmail, text)
                    .or()
                    .like(User::getPhone, text));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }
        Long total = userMapper.selectCount(wrapper);
        long offset = (safePage - 1) * safeSize;
        wrapper.orderByDesc(User::getCreateTime)
                .last("LIMIT " + safeSize + " OFFSET " + offset);

        List<UserVO> list = userMapper.selectList(wrapper).stream()
                .map(this::toUserVO)
                .collect(Collectors.toList());
        return new PageVO<>(list, total, safePage, safeSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createAdminUser(AdminUserDTO dto) {
        if (dto == null || StringUtils.isBlank(dto.getUsername()) || StringUtils.isBlank(dto.getPassword())) {
            throw new RuntimeException("用户名和密码不能为空");
        }
        String username = dto.getUsername().trim();
        ensureUnique(username, dto.getEmail(), dto.getPhone(), null);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(resolveDisplayName(dto.getName(), dto.getDisplayName(), username));
        user.setEmail(blankToNull(dto.getEmail()));
        user.setPhone(blankToNull(dto.getPhone()));
        user.setAvatar(blankToNull(dto.getAvatar()));
        user.setIntroduction(blankToNull(dto.getIntroduction()));
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        user.setUserType(dto.getUserType() == null ? 1 : dto.getUserType());
        user.setRegisterChannel("admin");
        userMapper.insert(user);
        bindSingleRole(user.getId(), normalizeRole(dto.getRoleCode()));
        return toUserVO(userMapper.selectById(user.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateAdminUser(Long id, AdminUserDTO dto) {
        if (id == null || dto == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        String username = StringUtils.isBlank(dto.getUsername()) ? existing.getUsername() : dto.getUsername().trim();
        ensureUnique(username, dto.getEmail(), dto.getPhone(), id);

        User update = new User();
        update.setId(id);
        update.setUsername(username);
        if (StringUtils.isNotBlank(dto.getPassword())) {
            update.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getName() != null || dto.getDisplayName() != null) {
            update.setName(resolveDisplayName(dto.getName(), dto.getDisplayName(), username));
        }
        update.setEmail(blankToNull(dto.getEmail()));
        update.setPhone(blankToNull(dto.getPhone()));
        update.setAvatar(blankToNull(dto.getAvatar()));
        update.setIntroduction(blankToNull(dto.getIntroduction()));
        update.setStatus(dto.getStatus());
        update.setUserType(dto.getUserType());
        userMapper.updateById(update);
        if (StringUtils.isNotBlank(dto.getRoleCode())) {
            bindSingleRole(id, normalizeRole(dto.getRoleCode()));
        }
        return toUserVO(userMapper.selectById(id));
    }

    @Override
    public boolean updateAdminUserStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAdminUser(Long id) {
        if (id == null) {
            return false;
        }
        authMapper.deleteUserRoles(id);
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean resetAdminUserPassword(Long id, String password) {
        if (id == null || StringUtils.isBlank(password)) {
            return false;
        }
        User user = new User();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(password));
        return userMapper.updateById(user) > 0;
    }

    private void ensureUnique(String username, String email, String phone, Long excludeId) {
        if (StringUtils.isBlank(username)) {
            throw new RuntimeException("用户名不能为空");
        }
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username);
        if (excludeId != null) {
            usernameWrapper.ne(User::getId, excludeId);
        }
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        checkUniqueContact(User::getEmail, email, excludeId, "邮箱已存在");
        checkUniqueContact(User::getPhone, phone, excludeId, "手机号已存在");
    }

    private void checkUniqueContact(com.baomidou.mybatisplus.core.toolkit.support.SFunction<User, ?> column,
                                    String value,
                                    Long excludeId,
                                    String message) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().eq(column, value.trim());
        if (excludeId != null) {
            wrapper.ne(User::getId, excludeId);
        }
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException(message);
        }
    }

    private void bindSingleRole(Long userId, String roleCode) {
        authMapper.deleteUserRoles(userId);
        Long roleId = authMapper.selectRoleIdByCode(roleCode);
        if (roleId != null) {
            authMapper.insertUserRole(userId, userId, roleId);
        }
    }

    private String normalizeRole(String roleCode) {
        return StringUtils.isBlank(roleCode) ? RoleCodes.USER : roleCode.trim().toUpperCase();
    }

    private String resolveDisplayName(String name, String displayName, String username) {
        if (StringUtils.isNotBlank(name)) {
            return name.trim();
        }
        if (StringUtils.isNotBlank(displayName)) {
            return displayName.trim();
        }
        return username;
    }

    private String blankToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        List<String> roles = authMapper.selectRoleCodesByUserId(user.getId());
        vo.setRoles(roles);
        vo.setRole(roles == null || roles.isEmpty() ? RoleCodes.USER : roles.get(0));
        vo.setPermissions(authMapper.selectPermissionCodesByUserId(user.getId()));
        return vo;
    }
}
