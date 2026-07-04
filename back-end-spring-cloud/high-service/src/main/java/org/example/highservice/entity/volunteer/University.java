/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 大学表实体
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("gaokao_university")
public class University {

    @TableId(type = IdType.ASSIGN_ID)
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