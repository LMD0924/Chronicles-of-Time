/**
 * 文件说明：高中模块 AI 分析请求 DTO，承接选科、专业和志愿场景的结构化上下文。
 */
package org.example.highservice.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HighAiAnalyzeRequest {

    private Long userId;
    private String scenario;
    private Map<String, Object> profile = new LinkedHashMap<>();
    private List<Map<String, Object>> candidates = new ArrayList<>();
    private String question;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Map<String, Object> getProfile() {
        return profile;
    }

    public void setProfile(Map<String, Object> profile) {
        this.profile = profile == null ? new LinkedHashMap<>() : profile;
    }

    public List<Map<String, Object>> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Map<String, Object>> candidates) {
        this.candidates = candidates == null ? new ArrayList<>() : candidates;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
