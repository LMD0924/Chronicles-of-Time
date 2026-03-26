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
