/**
 * 文件说明：拾光记微服务后端公共数据库通用工具源码，负责通用工具相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.commondb.utils;

/*
 * @Author:总会落叶
 * @Date:2026/3/19
 * @Description:
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 全局统一响应体 RestBean
 * 所有接口返回数据都通过该类封装，保证格式统一
 * 放置在 common-core 模块 → 通用实体包下
 */
/**
 * 类说明：当前类是通用工具模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestBean<T> implements Serializable {

    //响应状态码（200成功，4xx客户端错误，5xx服务器错误）
    private int code;
    //响应信息
    private String msg;
    //响应数据
    private T data;

    // ========== 静态构造方法（简化调用） ==========
    /**
     * 成功响应（无数据）
     */
    public static <T> RestBean<T> success(){
        return new RestBean<>(200, "操作成功", null);
    }

    /**
     * 成功响应，带数据
     */
    public static <T> RestBean<T> success(T data){
        return new RestBean<>(200, "操作成功", data);
    }

    /**
     * 成功响应，自定义提示+数据
     */
    public static <T> RestBean<T> success(String msg,T data){
        return new RestBean<>(200,msg,data);
    }

    /**
     * 失败响应（自定义状态码+提示）
     */
    public static <T> RestBean<T> fail(int code, String msg) {
        return new RestBean<>(code, msg, null);
    }

    /**
     * 失败响应（默认500状态码）
     */
    public static <T> RestBean<T> fail(String msg) {
        return new RestBean<>(500, msg, null);
    }
}
