/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 选课变更历史实体类（显式映射版）
 */
package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("hs_selection_history")
public class CourseSelectionHistory {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("selection_id")
    private Long selectionId;

    @TableField("student_id")
    private Long studentId;

    @TableField("student_name")
    private String studentName;

    @TableField("change_type")
    private String changeType;

    @TableField("old_first_subject")
    private String oldFirstSubject;

    @TableField("new_first_subject")
    private String newFirstSubject;

    @TableField("old_second_subject_1")
    private String oldSecondSubject1;

    @TableField("new_second_subject_1")
    private String newSecondSubject1;

    @TableField("old_second_subject_2")
    private String oldSecondSubject2;

    @TableField("new_second_subject_2")
    private String newSecondSubject2;

    @TableField("change_reason")
    private String changeReason;

    private String approver;

    @TableField("approve_status")
    private Integer approveStatus;

    @TableField("approve_comment")
    private String approveComment;

    @TableField("change_time")
    private LocalDateTime changeTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}