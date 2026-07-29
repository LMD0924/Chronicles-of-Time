package org.example.workplaceservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "workplace.ai")
public class InterviewAiProperties {

    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4o-mini";
    private String apiKey = "";
    private Integer timeoutSeconds = 45;
    private Integer maxTokens = 1000;
    private Double temperature = 0.5;

    public boolean enabled() {
        return apiKey != null && !apiKey.isBlank();
    }
}