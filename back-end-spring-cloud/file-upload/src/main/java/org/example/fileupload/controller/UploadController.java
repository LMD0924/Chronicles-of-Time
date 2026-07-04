/**
 * 文件说明：拾光记微服务后端文件上传服务接口控制器源码，负责接口控制器相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
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
/**
 * 类说明：当前类是接口控制器模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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