package com.atomicnorth.hrm.tenant.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
public class EmpSeparationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Resignation_date_cannot_be_null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate resignationDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate exitInterviewHeldOn;

    private Boolean leaveEncashed;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Relieving_date_cannot_be_null")
    private LocalDate relievingDate;

    private String newWorkplace;

    private Long employeeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    private String feedback;

    private String reasonForLeaving;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedDate;

    private String updatedBy;

    private String empName;
}
