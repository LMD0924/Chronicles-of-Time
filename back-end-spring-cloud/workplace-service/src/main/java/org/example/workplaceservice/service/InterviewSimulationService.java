package org.example.workplaceservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workplaceservice.config.InterviewAiProperties;
import org.example.workplaceservice.dto.InterviewTurnRequest;
import org.example.workplaceservice.dto.InterviewTurnResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSimulationService {

    private final InterviewAiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public InterviewTurnResponse turn(InterviewTurnRequest request) {
        InterviewTurnRequest safeRequest = request == null ? new InterviewTurnRequest() : request;
        if (!StringUtils.hasText(safeRequest.getAnswer())) {
            return firstQuestion(safeRequest);
        }
        if (!properties.enabled()) {
            return fallback(safeRequest, false);
        }
        try {
            return callModel(safeRequest);
        } catch (Exception exception) {
            log.warn("AI interview turn failed, using rule-based feedback", exception);
            return fallback(safeRequest, false);
        }
    }

    private InterviewTurnResponse firstQuestion(InterviewTurnRequest request) {
        InterviewTurnResponse response = new InterviewTurnResponse();
        response.setAiEnabled(properties.enabled());
        response.setQuestion(questionFor(request, Math.max(1, request.getRound() == null ? 1 : request.getRound())));
        response.setSummary(properties.enabled() ? "AI interviewer is ready." : "Rule-based interviewer is ready. Configure an API key for AI scoring.");
        response.setNextFocus("Use STAR: situation, task, action, result.");
        return response;
    }

    private InterviewTurnResponse callModel(InterviewTurnRequest request) throws Exception {
        JsonNode content = invokeModel(request);
        InterviewTurnResponse response = new InterviewTurnResponse();
        response.setAiEnabled(true);
        response.setScore(content.path("score").isInt() ? content.path("score").asInt() : 0);
        response.setSummary(content.path("summary").asText("Answer evaluated."));
        response.setStrengths(readList(content.path("strengths")));
        response.setImprovements(readList(content.path("improvements")));
        response.setNextFocus(content.path("nextFocus").asText("Use a concrete example and measurable result."));
        response.setQuestion(content.path("nextQuestion").asText(questionFor(request, Math.max(2, value(request.getRound()) + 1))));
        return response;
    }

    private JsonNode invokeModel(InterviewTurnRequest request) throws Exception {
        var root = objectMapper.createObjectNode();
        root.put("model", properties.getModel());
        root.put("temperature", properties.getTemperature());
        root.put("max_tokens", properties.getMaxTokens());
        var messages = root.putArray("messages");
        messages.addObject().put("role", "system").put("content", "You are a rigorous but supportive Chinese interview coach. Return only valid JSON with fields score (0-100 integer), summary, strengths (string array), improvements (string array), nextFocus, nextQuestion. Ask one concise next question.");
        messages.addObject().put("role", "user").put("content", prompt(request));

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(trimBaseUrl() + "/chat/completions"))
                .timeout(Duration.ofSeconds(Math.max(10, properties.getTimeoutSeconds())))
                .header("Authorization", "Bearer " + properties.getApiKey().trim())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(root)))
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
            throw new IllegalStateException("AI provider returned HTTP " + httpResponse.statusCode());
        }
        String modelContent = objectMapper.readTree(httpResponse.body())
                .path("choices").path(0).path("message").path("content").asText();
        if (!StringUtils.hasText(modelContent)) {
            throw new IllegalStateException("AI provider returned empty content");
        }
        return objectMapper.readTree(stripCodeFence(modelContent));
    }

    private InterviewTurnResponse fallback(InterviewTurnRequest request, boolean aiEnabled) {
        String answer = request.getAnswer().trim();
        int score = Math.min(92, 42 + Math.min(28, answer.length() / 8)
                + (containsAny(answer, "我", "负责", "行动", "结果", "提升", "降低", "%", "用户") ? 12 : 0)
                + (containsAny(answer, "数据", "指标", "复盘", "协作") ? 8 : 0));
        InterviewTurnResponse response = new InterviewTurnResponse();
        response.setAiEnabled(aiEnabled);
        response.setScore(score);
        response.setSummary(score >= 75 ? "回答结构较完整，已经体现了行动和结果。" : "回答有方向，但还需要把个人行动和结果说得更具体。");
        response.setStrengths(List.of(answer.length() >= 80 ? "回答具备基本展开，信息量足够。" : "回答简洁，主题没有明显跑偏。",
                containsAny(answer, "结果", "提升", "数据") ? "出现了结果或量化意识。" : "能围绕问题表达个人经历。"));
        List<String> improvements = new ArrayList<>();
        if (answer.length() < 120) improvements.add("补充背景、你的职责和关键行动，避免只给结论。");
        if (!containsAny(answer, "结果", "提升", "降低", "%", "数据")) improvements.add("加入可验证的结果，例如效率、质量、成本或用户反馈的变化。");
        if (improvements.isEmpty()) improvements.add("再压缩为 1 分钟版本，先结论后证据。");        response.setImprovements(improvements);
        response.setNextFocus("下一题继续使用 STAR，并说明你亲自做了什么。");
        response.setQuestion(questionFor(request, Math.max(2, value(request.getRound()) + 1)));
        return response;
    }

    private String prompt(InterviewTurnRequest request) {
        return "Position: " + blank(request.getPositionName(), "目标岗位")
                + "\nIndustry: " + blank(request.getIndustry(), "未指定")
                + "\nInterview type: " + blank(request.getInterviewType(), "综合面试")
                + "\nQuestion: " + blank(request.getPreviousQuestion(), "请介绍一个你解决问题的案例")
                + "\nCandidate answer: " + request.getAnswer();
    }

    private String questionFor(InterviewTurnRequest request, int round) {
        String position = blank(request.getPositionName(), "这个岗位");
        return switch (round) {
            case 1 -> "请用 1 分钟介绍自己，并说明为什么你适合 " + position + "？";
            case 2 -> "请讲一个你主导解决复杂问题的案例：当时的目标、行动和结果分别是什么？";
            case 3 -> "当你的方案被质疑或推进受阻时，你如何与相关方沟通并推动结果？";
            default -> "结合 " + position + " 的职责，你入职前三个月最想交付什么成果？为什么？";
        };
    }

    private List<String> readList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> { if (StringUtils.hasText(item.asText())) values.add(item.asText()); });
        }
        return values;
    }

    private boolean containsAny(String value, String... tokens) {
        for (String token : tokens) if (value.contains(token)) return true;
        return false;
    }

    private String trimBaseUrl() {
        String baseUrl = blank(properties.getBaseUrl(), "https://api.openai.com/v1");
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String blank(String value, String fallback) { return StringUtils.hasText(value) ? value.trim() : fallback; }
    private int value(Integer value) { return value == null ? 0 : value; }
    private String stripCodeFence(String value) { return value.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim(); }
}