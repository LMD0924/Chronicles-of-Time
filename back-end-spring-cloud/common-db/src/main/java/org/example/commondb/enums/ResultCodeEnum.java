/**
 * 文件说明：拾光记微服务后端公共数据库拾光记项目源码，负责拾光记项目相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.commondb.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */
/**
 * 类说明：当前类是拾光记项目模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Getter
public enum ResultCodeEnum {
    // 成功
    SUCCESS(200, "操作成功"),
    // 客户端错误
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或token过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    // 服务端错误
    SERVER_ERROR(500, "服务器内部错误"),
    // 业务错误（auth-center 专属）
    LOGIN_FAIL(10001, "账号或密码错误"),
    REGISTER_FAIL(10002, "注册失败，账号已存在"),
    USER_DISABLED(10003, "用户已被禁用，请联系管理员");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String msg;
    ResultCodeEnum(Integer code,String msg){
        this.code = code;
        this.msg = msg;
    }
}
