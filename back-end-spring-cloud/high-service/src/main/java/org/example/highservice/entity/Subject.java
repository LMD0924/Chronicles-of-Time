package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 科目基础信息实体类
 */
@Data
@TableName("subject")
public class Subject {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 科目代码（如：CHN001）
     */
    private String code;

    /**
     * 科目名称
     */
    private String name;

    /**
     * 科目类别：1-语文数学英语(必考) 2-物理/历史(二选一) 3-化学/生物/政治/地理(四选二)
     */
    private Integer category;

    /**
     * 类别名称：必考/首选/再选
     */
    private String categoryName;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 科目描述
     */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}