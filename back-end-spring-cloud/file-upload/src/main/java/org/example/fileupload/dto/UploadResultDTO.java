/**
 * 文件说明：拾光记微服务后端文件上传服务请求数据传输源码，负责请求数据传输相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.fileupload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author:总会落叶
 * @Date:2026/3/26
 * @Description:
 */
/**
 * 类说明：当前类是请求数据传输模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultDTO {

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 存储文件名
     */
    private String storedFilename;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小 (字节)
     */
    private Long fileSize;

    /**
     * 访问URL
     */
    private String url;

    /**
     * 缩略图URL (图片专用)
     */
    private String thumbnailUrl;
}
