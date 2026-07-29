package org.example.workplaceservice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InterviewTurnResponse {

    private String question;
    private Integer score;
    private String summary;
    private List<String> strengths = new ArrayList<>();
    private List<String> improvements = new ArrayList<>();
    private String nextFocus;
    private Boolean aiEnabled;
}