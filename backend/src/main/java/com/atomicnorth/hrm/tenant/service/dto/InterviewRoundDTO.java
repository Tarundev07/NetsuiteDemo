package com.atomicnorth.hrm.tenant.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
public class InterviewRoundDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long interviewRoundId;
    @NotBlank(message = "Interview_Round_Name_cannot_be_blank")
    private String interviewRoundName;
    @NotNull(message = "Interview_Type_ID_cannot_be_null")
    private Long interviewTypeId;
    @NotNull(message = "Interviewer_ID_cannot_be_null")
    private List<Long> interviewersId;
    @NotNull(message = "Designation_ID_cannot_be_null")
    private Long designationId;
    private List<Long> skillsId;
    @NotNull(message = "Is_Active_cannot_be_null")
    private Boolean isActive;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedDate;
    private String updatedBy;

    private String interviewTypeName;
    private List<String> interviewersName;
    private String designationName;
    private List<String> skillsName;
}
