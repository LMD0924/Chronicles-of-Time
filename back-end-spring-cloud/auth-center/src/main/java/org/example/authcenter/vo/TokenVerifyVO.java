/**
 * 文件说明：拾光记微服务后端认证中心响应视图数据源码，负责响应视图数据相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.authcenter.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author:总会落叶
 * @Date:2026/3/25
 * @Description: Token验证响应VO
 */
/**
 * 类说明：当前类是响应视图数据模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerifyVO {

    private Boolean valid;           // 是否有效
    private Long userId;             // 用户ID
    private String username;         // 用户名
    private String role;             // 角色
    private String message;          // 错误信息
}