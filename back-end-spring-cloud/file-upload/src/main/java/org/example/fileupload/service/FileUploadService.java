package org.example.fileupload.service;

import org.example.fileupload.dto.UploadResultDTO;
import org.springframework.web.multipart.MultipartFile;
/*
 * @Author:总会落叶
 * @Date:2026/3/26
 * @Description:
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