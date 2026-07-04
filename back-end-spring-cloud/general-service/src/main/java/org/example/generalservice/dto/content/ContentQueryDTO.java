/**
 * 文件说明：拾光记微服务后端通用内容服务内容社区源码，负责内容社区相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.dto.content;

import lombok.Data;
import java.util.List;

/**
 * 内容查询DTO
 *
 * @author 总会落叶
 * @date 2026-04-06
 */
/**
 * 类说明：当前类是内容社区模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
public class ContentQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 分类
     */
    private String category;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 搜索关键词
     */
    private String keyword;
}