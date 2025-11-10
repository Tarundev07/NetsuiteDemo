package com.atomicnorth.hrm.tenant.domain.jobOpening;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class JobRequisitionDTO extends AbstractAuditingEntity<Integer> implements Serializable {

    private Integer id;

    @NotNull(message = "Job title is required.")
    private String jobTitle;

    @NotNull(message = "Designation ID is required.")
    private Integer designationId;
    private String designationName;

    @NotNull(message = "Position requirement is required.")
    private Integer positionReq;

    @NotNull(message = "Expected salary is required.")
    private Integer expectedSalary;

    @NotNull(message = "Department ID is required.")
    private Long departmentId;
    private String departmentName;

    @NotNull(message = "Job description is required.")
    private String jobDescription;

    private String reasonRequest;

    @NotNull(message = "Requested by field is required.")
    private Integer requestedBy;
    private String requestedByName;

    @NotNull(message = "Requested department ID is required.")
    private Long requestedDepId;

    @NotNull(message = "Requested designation ID is required.")
    private Integer requestedDesgId;

    @NotNull(message = "Posted on is required.")
    private LocalDate postedOn;

    @NotNull(message = "Closed on is required.")
    private LocalDate closesOn;

    @NotNull(message = "Status is required.")
    private String isActive;
}

