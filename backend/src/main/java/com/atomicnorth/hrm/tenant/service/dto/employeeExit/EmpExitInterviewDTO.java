package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmpExitInterviewDTO {
    private Integer id;
    private List<Integer> interviewerId;
    private LocalDate interviewDate;
    private String feedback;
    private String status;
    private String finalComments;
    private String suggestions;
    private String exitInterviewStatus;
    private String employeeName;
    private String exitRequestNumber;
    private String department;
    private String designation;
    private String reportingManager;
    private LocalDate joiningDate;
    private LocalDate lastWorkingDate;
    private String resignationType;
    private String reason;
    private Integer noticePeriod;
    private String interviewType;
    private String interviewStatus;
    private List<EmpExitInterviewFeedbackDTO> feedbackDTOList;
}
