/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 学生选课记录实体类（100%匹配数据库）
 */
package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student_course_selection")
public class StudentCourseSelection {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long studentId;
    private String studentName;
    private String grade;
    private String className;
    private String academicYear;
    private String semester;

    private BigDecimal chineseScore;
    private BigDecimal mathScore;
    private BigDecimal englishScore;

    private Long firstSubjectId;
    private String firstSubjectName;
    private BigDecimal firstSubjectScore;
    private String firstSubjectLevel;

    // ====================== 再选科目1（精准匹配）======================
    @TableField("second_subject_1_id")
    private Long secondSubject1Id;

    @TableField("second_subject_1_name")
    private String secondSubject1Name;

    @TableField("second_subject_1_score")
    private BigDecimal secondSubject1Score;

    @TableField("second_subject_1_level")
    private String secondSubject1Level;

    // ====================== 再选科目2（精准匹配）======================
    @TableField("second_subject_2_id")
    private Long secondSubject2Id;

    @TableField("second_subject_2_name")
    private String secondSubject2Name;

    @TableField("second_subject_2_score")
    private BigDecimal secondSubject2Score;

    @TableField("second_subject_2_level")
    private String secondSubject2Level;

    private Long combinationId;
    private String combinationName;
    private BigDecimal totalScore;
    private BigDecimal totalScoreWeighted;
    private Integer classRank;
    private Integer gradeRank;
    private Integer status;

    private Boolean isConfirmed;
    private LocalDateTime confirmTime;
    private String selectionReason;
    private String futurePlan;
    private String teacherAdvice;
    private String parentOpinion;

    // ====================== 【关键修复】is_public ======================
    // 数据库是 varchar，所以实体类必须用 String
    private Boolean isPublic;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段
    @TableField(exist = false)
    private Subject firstSubject;
    @TableField(exist = false)
    private Subject secondSubject1;
    @TableField(exist = false)
    private Subject secondSubject2;
    @TableField(exist = false)
    private SubjectCombination combination;
}