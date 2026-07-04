/**
 * 文件说明：拾光记微服务后端文件上传服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.fileupload.service;

import org.example.fileupload.dto.UploadResultDTO;
import org.springframework.web.multipart.MultipartFile;
/*
 * @Author:总会落叶
 * @Date:2026/3/26
 * @Description:
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
public interface FileUploadService {

    /**
     * 上传文件
     */
    UploadResultDTO uploadFile(MultipartFile file);

    /**
     * 上传图片
     */
    UploadResultDTO uploadImage(MultipartFile file);

    /**
     * 删除文件
     */
    boolean deleteFile(String fileId);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String storedFilename);

    /**
     * 生成缩略图
     */
    String generateThumbnail(String imagePath, String filename) throws Exception;
}