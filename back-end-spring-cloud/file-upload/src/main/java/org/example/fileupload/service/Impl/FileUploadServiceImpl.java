package org.example.fileupload.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.example.fileupload.config.FileUploadConfig;
import org.example.fileupload.dto.UploadResultDTO;
import org.example.fileupload.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final FileUploadConfig fileUploadConfig;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public UploadResultDTO uploadFile(MultipartFile file) {
        return upload(file, false);
    }

    @Override
    public UploadResultDTO uploadImage(MultipartFile file) {
        return upload(file, true);
    }

    private UploadResultDTO upload(MultipartFile file, boolean isImage) {
        // 1. 验证文件
        validateFile(file);

        String contentType = file.getContentType();

        // 2. 验证文件类型
        if (!fileUploadConfig.isValidFileType(contentType)) {
            throw new RuntimeException("不支持的文件类型: " + contentType);
        }

        // 3. 获取存储路径
        String subPath = fileUploadConfig.getSubPathByContentType(contentType);
        String datePath = LocalDate.now().format(DATE_FORMAT);
        String fullPath = fileUploadConfig.getFullPath(subPath) + datePath + "/";

        // 4. 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + extension;

        // 5. 创建目录并保存文件
        try {
            Path uploadDir = Paths.get(fullPath);

            // ✅ 打印日志查看路径
            log.info("上传目录: {}", uploadDir.toAbsolutePath());

            // ✅ 使用 createDirectories 递归创建目录
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建目录成功: {}", uploadDir.toAbsolutePath());
            }

            Path filePath = uploadDir.resolve(storedFilename);
            file.transferTo(filePath.toFile());

            log.info("文件保存成功: {}", filePath.toAbsolutePath());

            // 6. 构建返回结果
            // 注意：磁盘上文件路径包含 subPath + datePath，
            // 而 WebConfig 映射 /files/** -> uploads 目录，所以 URL 也必须包含这些相对路径。
            String fileUrl = fileUploadConfig.getAccessUrl() + subPath + datePath + "/" + storedFilename;
            UploadResultDTO result = UploadResultDTO.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .fileType(contentType)
                    .fileSize(file.getSize())
                    .url(fileUrl)
                    .build();

            // 7. 生成缩略图（图片专用）
            if (isImage && fileUploadConfig.getGenerateThumbnail()) {
                try {
                    String thumbnailFilename = generateThumbnail(fullPath, storedFilename);
                    String thumbnailUrl = fileUploadConfig.getAccessUrl() + subPath + datePath + "/" + thumbnailFilename;
                    result.setThumbnailUrl(thumbnailUrl);
                } catch (Exception e) {
                    log.warn("生成缩略图失败: {}", e.getMessage());
                }
            }

            return result;

        } catch (IOException e) {
            log.error("文件保存失败，目标路径: {}", fullPath, e);
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        long maxSize = fileUploadConfig.getMaxFileSize() * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("文件大小不能超过 " + fileUploadConfig.getMaxFileSize() + "MB");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    @Override
    public boolean deleteFile(String fileId) {
        return true;
    }

    @Override
    public String getFileUrl(String storedFilename) {
        return fileUploadConfig.getAccessUrl() + storedFilename;
    }

    @Override
    public String generateThumbnail(String imagePath, String filename) throws Exception {
        String thumbnailFilename = "thumb_" + filename;
        String thumbnailFullPath = imagePath + thumbnailFilename;

        Thumbnails.of(imagePath + filename)
                .size(fileUploadConfig.getThumbnailWidth(), fileUploadConfig.getThumbnailHeight())
                .toFile(thumbnailFullPath);

        return thumbnailFilename;
    }
}