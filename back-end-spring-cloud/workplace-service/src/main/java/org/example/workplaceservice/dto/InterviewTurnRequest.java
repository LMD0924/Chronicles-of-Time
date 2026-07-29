package org.example.workplaceservice.dto;

import lombok.Data;

@Data
public class InterviewTurnRequest {

    private String positionName;
    private String industry;
    private String interviewType;
    private Integer round;
    private String previousQuestion;
    private String answer;
}