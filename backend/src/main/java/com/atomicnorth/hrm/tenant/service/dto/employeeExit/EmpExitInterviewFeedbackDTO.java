package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;

@Data
public class EmpExitInterviewFeedbackDTO {
    private Integer interviewFeedbackId;
    private Integer interviewId;
    private String feedbackType;
    private String feedbackValue;
}
