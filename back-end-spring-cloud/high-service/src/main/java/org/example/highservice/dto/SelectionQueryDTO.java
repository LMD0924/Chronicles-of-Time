/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 选课查询DTO
 */
package org.example.highservice.dto;

import lombok.Data;

@Data
public class SelectionQueryDTO {
    /**
     * 学生ID
     */
    private Long studentId;

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