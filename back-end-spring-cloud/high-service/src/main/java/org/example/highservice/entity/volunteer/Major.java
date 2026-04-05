package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 专业表实体
 */
@Data
@TableName("major")
public class Major {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String name;

    private String category;

    private String subCategory;

    private Integer duration;

    private BigDecimal tuitionFee;

    private String description;
}