package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("gaokao_major")
public class Major {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("major_code")
    private String code;

    @TableField("major_name")
    private String name;

    private String category;
    private String subCategory;

    @TableField("duration_years")
    private Integer duration;

    private BigDecimal tuitionFee;
    private String description;
}