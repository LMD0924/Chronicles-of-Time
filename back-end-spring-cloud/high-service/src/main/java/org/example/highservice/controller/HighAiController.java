/**
 * 文件说明：高中模块 AI 分析接口，面向志愿、选科和专业方向提供统一分析入口。
 */
package org.example.highservice.controller;

import org.example.commondb.utils.RestBean;
import org.example.highservice.dto.HighAiAnalyzeRequest;
import org.example.highservice.dto.HighAiAnalyzeResponse;
import org.example.highservice.service.HighAiAnalysisService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/high/ai")
public class HighAiController {

    private final HighAiAnalysisService highAiAnalysisService;

    public HighAiController(HighAiAnalysisService highAiAnalysisService) {
        this.highAiAnalysisService = highAiAnalysisService;
    }

    @PostMapping("/analyze")
    public RestBean<HighAiAnalyzeResponse> analyze(@RequestBody HighAiAnalyzeRequest request) {
        return RestBean.success("分析完成", highAiAnalysisService.analyze(request));
    }
}
