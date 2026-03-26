package org.example.fileupload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /** 上传根目录 */
    private String basePath = "./uploads/";

    /** 访问URL前缀 */
    private String accessUrl = "http://localhost:8090/files/";

    /** 图片子目录 */
    private String imagePath = "images/";

    /** 视频子目录 */
    private String videoPath = "videos/";

    /** 文档子目录 */
    private String documentPath = "documents/";

    /** 其他文件子目录 */
    private String otherPath = "others/";

    /** 允许的图片类型 */
    private List<String> allowedImageTypes;

    /** 允许的视频类型 */
    private List<String> allowedVideoTypes;

    /** 允许的文档类型 */
    private List<String> allowedDocumentTypes;

    /** 最大文件大小 (MB) */
    private Long maxFileSize = 10L;

    /** 是否生成缩略图 */
    private Boolean generateThumbnail = true;

    /** 缩略图宽度 */
    private Integer thumbnailWidth = 200;

    /** 缩略图高度 */
    private Integer thumbnailHeight = 200;

    /**
     * 获取完整的上传路径
     */
    public String getFullPath(String subPath) {
        return basePath + subPath;
    }

    /**
     * 根据文件类型获取子目录
     */
    public String getSubPathByContentType(String contentType) {
        if (contentType == null) {
            return otherPath;
        }

        if (contentType.startsWith("image/")) {
            return imagePath;
        } else if (contentType.startsWith("video/")) {
            return videoPath;
        } else if (isDocumentType(contentType)) {
            return documentPath;
        } else {
            return otherPath;
        }
    }

    /**
     * 判断是否为文档类型
     */
    private boolean isDocumentType(String contentType) {
        return allowedDocumentTypes != null && allowedDocumentTypes.contains(contentType);
    }

    /**
     * 验证文件类型是否允许
     */
    public boolean isValidFileType(String contentType) {
        if (contentType == null) {
            return false;
        }

        return (allowedImageTypes != null && allowedImageTypes.contains(contentType)) ||
                (allowedVideoTypes != null && allowedVideoTypes.contains(contentType)) ||
                (allowedDocumentTypes != null && allowedDocumentTypes.contains(contentType));
    }
}