package org.example.authcenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.authcenter.entity.User;
import org.example.authcenter.vo.UserVO;

/*
 * @Author:总会落叶
 * @Date:2026/3/24
 * @Description:
 */
public interface UserService extends IService<User> {

    /**
     * 根据id获取用户信息
     */
    User getUserById(Long id);
}
