package org.example.fileupload.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.fileupload.dto.UploadResultDTO;
import org.example.fileupload.service.FileUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
/*
 * @Author:总会落叶
 * @Date:2026/3/26
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileUploadService fileUploadService;

    /**
     * 通用文件上传
     */
    @PostMapping("/file")
    public RestBean<UploadResultDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            UploadResultDTO result = fileUploadService.uploadFile(file);
            return RestBean.success(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 图片上传（自动生成缩略图）
     */
    @PostMapping("/image")
    public RestBean<UploadResultDTO> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            UploadResultDTO result = fileUploadService.uploadImage(file);
            return RestBean.success(result);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 多文件上传
     */
    @PostMapping("/files")
    public RestBean<List<UploadResultDTO>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            List<UploadResultDTO> results = new ArrayList<>();
            for (MultipartFile file : files) {
                UploadResultDTO result = fileUploadService.uploadFile(file);
                results.add(result);
            }
            return RestBean.success(results);
        } catch (Exception e) {
            log.error("批量上传失败", e);
            return RestBean.fail(e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file/{fileId}")
    public RestBean<String> deleteFile(@PathVariable String fileId) {
        try {
            boolean success = fileUploadService.deleteFile(fileId);
            if (success) {
                return RestBean.success("删除成功");
            }
            return RestBean.fail("删除失败");
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return RestBean.fail(e.getMessage());
        }
    }
}