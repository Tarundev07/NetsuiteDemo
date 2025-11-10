package com.atomicnorth.hrm.tenant.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
public class InterviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long interviewId;
    @NotNull(message = "Interview_Round_Id_cannot_be_null")
    private Long interviewRoundId;
    private List<Long> interviewerId;
    private String status;
    @NotNull(message = "Job_Applicant_Id_cannot_be_blank")
    private Integer jobApplicantId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Interview_scheduled_date_is_required")
    private LocalDate interviewScheduledDate;
    @NotBlank(message = "From_time_cannot_be_blank")
    private String fromTime;
    @NotBlank(message = "To_time_cannot_be_blank")
    private String toTime;
    private Long rating;
    private String summary;
    private String resumeLink;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedDate;
    private String updatedBy;

    private String interviewRoundName;
    private String jobApplicantName;
    private List<String> interviewersName;
}
