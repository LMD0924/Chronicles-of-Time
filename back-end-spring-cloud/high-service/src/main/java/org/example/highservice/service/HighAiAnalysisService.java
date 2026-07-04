/**
 * 文件说明：高中模块 AI 分析服务，负责调用大模型并在未配置密钥时提供本地规则分析。
 */
package org.example.highservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.highservice.config.HighAiProperties;
import org.example.highservice.dto.HighAiAnalyzeRequest;
import org.example.highservice.dto.HighAiAnalyzeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class HighAiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(HighAiAnalysisService.class);
    private static final String LOCAL_MODEL = "local-rules";

    private final HighAiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public HighAiAnalysisService(HighAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(5, properties.getTimeoutSeconds())))
                .build();
    }

    public HighAiAnalyzeResponse analyze(HighAiAnalyzeRequest request) {
        HighAiAnalyzeRequest safeRequest = request == null ? new HighAiAnalyzeRequest() : request;
        String scenario = normalizeScenario(safeRequest.getScenario());

        if (!properties.isEnabled()) {
            return fallback(safeRequest, scenario, "AI 分析已关闭，当前使用本地规则分析。");
        }
        if (!properties.hasApiKey()) {
            return fallback(safeRequest, scenario, "未配置大模型 API Key，当前使用本地规则分析。");
        }

        try {
            HighAiAnalyzeResponse response = callModel(safeRequest, scenario);
            response.setScenario(scenario);
            response.setAiEnabled(true);
            response.setModel(properties.getModel());
            response.setProviderStatus("大模型分析");
            return response;
        } catch (Exception e) {
            log.warn("High AI analyze failed, fallback to local rules. scenario={}", scenario, e);
            return fallback(safeRequest, scenario, "大模型调用失败，当前使用本地规则分析。");
        }
    }

    private HighAiAnalyzeResponse callModel(HighAiAnalyzeRequest request, String scenario) throws Exception {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", properties.getModel());
        requestBody.put("temperature", properties.getTemperature());
        requestBody.put("max_tokens", properties.getMaxTokens());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt()));
        messages.add(message("user", buildUserPrompt(request, scenario)));
        requestBody.put("messages", messages);

        String body = objectMapper.writeValueAsString(requestBody);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(properties.chatCompletionsUrl()))
                .timeout(Duration.ofSeconds(Math.max(5, properties.getTimeoutSeconds())))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + properties.getApiKey().trim())
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
            throw new IllegalStateException("AI provider returned HTTP " + httpResponse.statusCode());
        }

        JsonNode root = objectMapper.readTree(httpResponse.body());
        String content = root.path("choices").path(0).path("message").path("content").asText("");
        if (content == null || content.isBlank()) {
            content = root.path("choices").path(0).path("text").asText("");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("AI provider response is empty");
        }
        return parseModelContent(content, scenario);
    }

    private String systemPrompt() {
        return """
                你是面向中国高中生的新高考升学规划助手。请基于用户提供的数据，分析志愿填报、选科组合或专业方向。
                要求：结论谨慎、可执行，不编造录取分数线，不替代官方招生章程；如果数据不足，明确说明需要补充哪些信息。
                只返回 JSON，不要使用 Markdown。JSON 字段必须为：
                {
                  "summary": "总体结论",
                  "strengths": ["优势1", "优势2"],
                  "risks": ["风险1", "风险2"],
                  "suggestions": ["建议1", "建议2"],
                  "actionPlan": "下一步行动计划"
                }
                """;
    }

    private String buildUserPrompt(HighAiAnalyzeRequest request, String scenario) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scenario", scenario);
        payload.put("profile", request.getProfile());
        payload.put("candidates", limitCandidates(request.getCandidates()));
        payload.put("question", request.getQuestion());
        return "请分析以下高中升学规划数据：" + objectMapper.writeValueAsString(payload);
    }

    private List<Map<String, Object>> limitCandidates(List<Map<String, Object>> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        return candidates.stream().limit(20).toList();
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private HighAiAnalyzeResponse parseModelContent(String content, String scenario) {
        HighAiAnalyzeResponse response = new HighAiAnalyzeResponse();
        response.setScenario(scenario);
        String jsonText = stripJsonFence(content);
        try {
            JsonNode node = objectMapper.readTree(jsonText);
            response.setSummary(text(node, "summary", content));
            response.setStrengths(arrayText(node.path("strengths")));
            response.setRisks(arrayText(node.path("risks")));
            response.setSuggestions(arrayText(node.path("suggestions")));
            response.setActionPlan(text(node, "actionPlan", ""));
        } catch (Exception ex) {
            response.setSummary(content);
            response.setStrengths(List.of());
            response.setRisks(List.of("模型返回不是标准 JSON，已按原文展示结论。"));
            response.setSuggestions(List.of("可以重新发起分析，或补充更明确的分数、排名、选科和候选专业信息。"));
            response.setActionPlan("先核对当前输入数据，再结合官方招生章程确认最终方案。");
        }
        return response;
    }

    private String stripJsonFence(String content) {
        String text = content == null ? "" : content.trim();
        if (text.startsWith("```")) {
            int firstLine = text.indexOf('\n');
            int lastFence = text.lastIndexOf("```");
            if (firstLine >= 0 && lastFence > firstLine) {
                text = text.substring(firstLine + 1, lastFence).trim();
            }
        }
        return text;
    }

    private String text(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? defaultValue : value.asText(defaultValue);
    }

    private List<String> arrayText(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return result;
        }
        node.forEach(item -> {
            String text = item.asText("");
            if (!text.isBlank()) {
                result.add(text);
            }
        });
        return result;
    }

    private HighAiAnalyzeResponse fallback(HighAiAnalyzeRequest request, String scenario, String status) {
        HighAiAnalyzeResponse response = switch (scenario) {
            case "subject_selection" -> fallbackSubjectSelection(request, scenario);
            case "major" -> fallbackMajor(request, scenario);
            default -> fallbackVolunteer(request, scenario);
        };
        response.setAiEnabled(false);
        response.setModel(LOCAL_MODEL);
        response.setProviderStatus(status);
        return response;
    }

    private HighAiAnalyzeResponse fallbackSubjectSelection(HighAiAnalyzeRequest request, String scenario) {
        Map<String, Object> profile = request.getProfile();
        List<String> subjects = subjectNames(profile);
        String plan = stringValue(profile, "futurePlan");

        HighAiAnalyzeResponse response = baseResponse(scenario);
        response.setSummary(subjects.isEmpty()
                ? "请先完成首选科目和再选科目选择，再进行选科分析。"
                : "当前组合为 " + String.join("、", subjects) + "，建议围绕目标专业要求和个人优势继续校验。");

        if (subjects.contains("物理")) {
            response.getStrengths().add("包含物理，理工类、计算机类、电子信息类等方向的基础覆盖面更好。");
        } else {
            response.getRisks().add("未选择物理，计算机、电子信息、自动化、机械等专业可能受限。");
        }
        if (subjects.contains("化学")) {
            response.getStrengths().add("包含化学，医学、药学、材料、化工等方向的兼容度更高。");
        } else {
            response.getRisks().add("未选择化学，医学、药学、化工、材料等专业需要重点核对选科要求。");
        }
        if (subjects.contains("历史")) {
            response.getStrengths().add("包含历史，更适合文史哲、法学、新闻传播、教育等偏人文方向。");
        }
        if (plan == null || plan.isBlank()) {
            response.getRisks().add("未来规划为空，选科判断缺少目标专业或目标院校参照。");
        }

        response.setSuggestions(List.of(
                "先列出 3 到 5 个目标专业，再逐个反查近年选科要求。",
                "把兴趣、优势学科、可接受专业范围分开评估，避免只按热门专业选科。",
                "如果目标偏医学、药学、材料或化工，优先核对是否必须选择化学。"
        ));
        response.setActionPlan("补充目标专业或大学，按“必须科目、推荐科目、个人成绩优势”三项重新确认组合。");
        return response;
    }

    private HighAiAnalyzeResponse fallbackMajor(HighAiAnalyzeRequest request, String scenario) {
        List<Map<String, Object>> candidates = request.getCandidates();
        HighAiAnalyzeResponse response = baseResponse(scenario);
        if (candidates == null || candidates.isEmpty()) {
            response.setSummary("当前没有可分析的专业候选，请先完成选科或搜索专业。");
            response.getRisks().add("专业候选为空，无法判断专业覆盖面和匹配度。");
        } else {
            response.setSummary("当前共有 " + candidates.size() + " 个专业候选，建议从匹配度、院校层次和长期兴趣三方面筛选。");
            response.getStrengths().add("已有候选专业列表，可以直接比较选科要求、院校层次和匹配度。");
            if (hasHighMatchingScore(candidates)) {
                response.getStrengths().add("候选列表中存在较高匹配度专业，可以作为优先调研方向。");
            }
            if (categoryCount(candidates) <= 2 && candidates.size() >= 5) {
                response.getRisks().add("候选专业类别较集中，需要确认是否过早收窄专业范围。");
            }
        }
        response.getSuggestions().add("优先保留匹配度高、选科要求明确、培养方向清晰的专业。");
        response.getSuggestions().add("对目标专业补充课程内容、就业方向、读研路径和院校层次信息。");
        response.getSuggestions().add("不要只按专业热度排序，应同时考虑个人学科优势和可持续学习压力。");
        response.setActionPlan("把候选专业分为重点了解、备选观察、暂不考虑三类，再进入志愿方案组合。");
        return response;
    }

    private HighAiAnalyzeResponse fallbackVolunteer(HighAiAnalyzeRequest request, String scenario) {
        Map<String, Object> profile = request.getProfile();
        List<Map<String, Object>> candidates = request.getCandidates();
        Map<String, Integer> strategyCount = countStrategies(candidates);

        HighAiAnalyzeResponse response = baseResponse(scenario);
        response.setSummary(candidates == null || candidates.isEmpty()
                ? "当前没有候选志愿，建议先生成推荐结果或补充志愿详情。"
                : "当前候选志愿 " + candidates.size() + " 个，需重点检查冲稳保梯度、选科匹配和专业接受度。");

        if (numberValue(profile, "score") != null) {
            response.getStrengths().add("已提供分数，可与近年录取线进行基础梯度判断。");
        } else {
            response.getRisks().add("未提供分数，无法进行录取风险分层。");
        }
        if (numberValue(profile, "rank") != null) {
            response.getStrengths().add("已提供位次，建议优先使用位次对比近年录取数据。");
        } else {
            response.getRisks().add("未提供位次，分数跨年份可比性有限。");
        }
        if (strategyCount.getOrDefault("保底", 0) == 0 && strategyCount.getOrDefault("稳妥", 0) == 0 && candidates != null && !candidates.isEmpty()) {
            response.getRisks().add("当前缺少明显的稳妥或保底志愿，滑档风险需要重点控制。");
        }
        if (strategyCount.getOrDefault("冲刺", 0) + strategyCount.getOrDefault("梦想", 0) > Math.max(2, strategyCount.getOrDefault("保底", 0) + 2)) {
            response.getRisks().add("冲刺和梦想志愿占比较高，建议补足稳妥和保底层。");
        }

        response.getSuggestions().add("按照“冲刺、稳妥、保底”建立梯度，不要只保留高热度院校。");
        response.getSuggestions().add("逐项核对专业选科要求、单科限制、体检限制、转专业政策和调剂规则。");
        response.getSuggestions().add("同一层级内优先选择专业接受度更高、往年位次波动更小的院校专业组。");
        response.setActionPlan("先用位次复核近三年录取数据，再补齐稳妥和保底项，最后做一次模拟录取和选科匹配检查。");
        return response;
    }

    private HighAiAnalyzeResponse baseResponse(String scenario) {
        HighAiAnalyzeResponse response = new HighAiAnalyzeResponse();
        response.setScenario(scenario);
        return response;
    }

    private String normalizeScenario(String scenario) {
        if (scenario == null || scenario.isBlank()) {
            return "volunteer";
        }
        String value = scenario.trim().toLowerCase();
        if ("selection".equals(value) || "course_selection".equals(value)) {
            return "subject_selection";
        }
        if ("plan".equals(value)) {
            return "volunteer";
        }
        return value;
    }

    private List<String> subjectNames(Map<String, Object> profile) {
        List<String> result = new ArrayList<>();
        addSubject(result, profile.get("firstSubject"));
        addSubject(result, profile.get("firstSubjectName"));
        addSubject(result, profile.get("secondSubject1"));
        addSubject(result, profile.get("secondSubject1Name"));
        addSubject(result, profile.get("secondSubject2"));
        addSubject(result, profile.get("secondSubject2Name"));
        Object subjects = profile.get("subjects");
        if (subjects instanceof Iterable<?> iterable) {
            iterable.forEach(item -> addSubject(result, item));
        }
        Object selectedSubjects = profile.get("selectedSubjects");
        if (selectedSubjects instanceof Iterable<?> iterable) {
            iterable.forEach(item -> addSubject(result, item));
        } else if (selectedSubjects instanceof String text) {
            for (String subject : text.split("[,，/、\\s]+")) {
                addSubject(result, subject);
            }
        }
        return result.stream().distinct().toList();
    }

    private void addSubject(List<String> result, Object value) {
        String text = Objects.toString(value, "").trim();
        if (!text.isEmpty() && !"null".equalsIgnoreCase(text)) {
            result.add(text);
        }
    }

    private String stringValue(Map<String, Object> profile, String key) {
        Object value = profile == null ? null : profile.get(key);
        return value == null ? null : Objects.toString(value, "");
    }

    private Number numberValue(Map<String, Object> profile, String key) {
        if (profile == null) {
            return null;
        }
        Object value = profile.get(key);
        if (value instanceof Number number) {
            return number;
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Double.parseDouble(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean hasHighMatchingScore(List<Map<String, Object>> candidates) {
        return candidates.stream().anyMatch(item -> {
            Object score = item.get("avgMatchingScore");
            if (!(score instanceof Number)) {
                score = item.get("matchingScore");
            }
            return score instanceof Number number && number.doubleValue() >= 75;
        });
    }

    private int categoryCount(List<Map<String, Object>> candidates) {
        return (int) candidates.stream()
                .map(item -> Objects.toString(item.get("category"), ""))
                .filter(text -> !text.isBlank())
                .distinct()
                .count();
    }

    private Map<String, Integer> countStrategies(List<Map<String, Object>> candidates) {
        Map<String, Integer> result = new HashMap<>();
        if (candidates == null) {
            return result;
        }
        for (Map<String, Object> candidate : candidates) {
            String strategy = Objects.toString(candidate.get("strategy"), "").trim();
            if (!strategy.isEmpty()) {
                result.merge(strategy, 1, Integer::sum);
            }
        }
        return result;
    }
}
