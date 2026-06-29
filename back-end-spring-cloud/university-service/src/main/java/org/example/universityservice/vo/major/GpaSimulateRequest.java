package org.example.universityservice.vo.major;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class GpaSimulateRequest {
    private Long majorId;
    private List<ScoreItem> scores = new ArrayList<>();

    @Data
    public static class ScoreItem {
        private Long courseId;
        private BigDecimal score;
    }
}
