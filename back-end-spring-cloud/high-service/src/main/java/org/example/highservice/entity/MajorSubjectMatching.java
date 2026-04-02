/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 专业与科目匹配度实体类
 */
package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("major_subject_matching")
public class MajorSubjectMatching {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 专业代码
     */
    private String majorCode;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 科目ID
     */
    private Long subjectId;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 重要程度：1-必须 2-强烈推荐 3-推荐 4-相关
     */
    private Integer importanceLevel;

    /**
     * 匹配度分数（0-100）
     */
    private Integer matchingScore;

    /**
     * 匹配说明
     */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}