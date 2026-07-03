/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 学科组合实体类（3+1+2所有可能组合）
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("hs_subject_combination")
public class SubjectCombination {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 组合代码（如：PHY_CHE_BIO）
     */
    private String code;

    /**
     * 组合名称（如：物化生）
     */
    private String name;

    /**
     * 首选科目ID（物理/历史）
     */
    private Long firstSubjectId;

    /**
     * 再选科目1 ID
     */
    private Long secondSubjectId1;

    /**
     * 再选科目2 ID
     */
    private Long secondSubjectId2;

    /**
     * 组合描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 热门程度排名
     */
    private Integer popularityRank;

    /**
     * 首选科目（非数据库字段）
     */
    @TableField(exist = false)
    private Subject firstSubject;

    /**
     * 再选科目1（非数据库字段）
     */
    @TableField(exist = false)
    private Subject secondSubject1;

    /**
     * 再选科目2（非数据库字段）
     */
    @TableField(exist = false)
    private Subject secondSubject2;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}