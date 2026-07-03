/**
 * 文件说明：拾光记微服务后端公共核心认证与登录源码，负责认证与登录相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.commoncore.auth;
/**
 * 类说明：当前类是认证与登录模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

public final class RoleCodes {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String TEACHER = "TEACHER";
    public static final String PARENT = "PARENT";

    private RoleCodes() {
    }
}
