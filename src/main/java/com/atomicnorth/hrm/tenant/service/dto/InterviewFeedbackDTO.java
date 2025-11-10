package com.atomicnorth.hrm.tenant.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
public class InterviewFeedbackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long interviewFeedbackId;
    @NotNull(message = "Interview ID cannot be null")
    private Long interviewId;
    @NotNull(message = "Job Applicant ID cannot be blank")
    private Integer jobApplicantId;
    private String interviewResultCode;
    @NotNull(message = "Is_Active_cannot_be_null")
    private Boolean isActive;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedDate;
    private String updatedBy;
    private Map<String, Integer> skillFeedback;
    private String jobApplicantName;
    private String interviewResult;

}
