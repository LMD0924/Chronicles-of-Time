package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("gaokao_university")
public class University {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("university_name")
    private String name;

    @TableField("university_code")
    private String code;

    private String province;
    private String city;

    @TableField("level_tags")
    private String level;

    @TableField("type_tags")
    private String type;

    private String website;
    private String logoUrl;
    private String description;

    @TableField("founded_year")
    private Integer establishedYear;

    private Boolean isPublic;
}