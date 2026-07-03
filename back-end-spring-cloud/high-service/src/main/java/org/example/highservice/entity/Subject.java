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
 * @Description: 科目基础信息实体类
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("hs_subject")
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