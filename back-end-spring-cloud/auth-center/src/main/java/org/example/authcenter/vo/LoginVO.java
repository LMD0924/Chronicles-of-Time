/**
 * 文件说明：拾光记微服务后端认证中心响应视图数据源码，负责响应视图数据相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 类说明：当前类是响应视图数据模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private List<String> roles;
    private List<String> permissions;
    private UserVO userInfo;
}
