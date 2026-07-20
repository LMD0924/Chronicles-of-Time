/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 选课查询DTO
 */
package org.example.highservice.dto;

import lombok.Data;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
public class SelectionQueryDTO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前用户ID（用于权限判断）
     */
    private Long currentUserId;

    /**
     * 年级
     */
    private String grade;

    /**
     * 班级
     */
    private String className;

    /**
     * 学年
     */
    private String academicYear;

    /**
     * 学期
     */
    private String semester;

    /**
     * 是否确认
     */
    private Boolean isConfirmed;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}