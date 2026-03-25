package org.example.commondb.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
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
