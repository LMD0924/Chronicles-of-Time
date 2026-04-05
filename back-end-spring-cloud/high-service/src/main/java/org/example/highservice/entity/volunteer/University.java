package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 大学表实体
 */
@Data
@TableName("university")
public class University {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String code;

    private String province;

    private String city;

    private String level; // 985,211,双一流,一本,二本,专科

    private String type; // 综合,理工,师范等

    private String website;

    private String logoUrl;

    private String description;

    private Integer establishedYear;

    private Boolean isPublic;
}