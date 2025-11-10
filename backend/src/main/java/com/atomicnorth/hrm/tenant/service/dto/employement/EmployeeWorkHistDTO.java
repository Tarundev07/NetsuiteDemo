package com.atomicnorth.hrm.tenant.service.dto.employement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

@Data
public class EmployeeWorkHistDTO extends AbstractAuditingEntity<Long> implements Serializable {

    private Integer employmentHistoryId;

    @NotNull(message = "username cannot be null")
    private Integer username;

    @NotBlank(message = "orgName cannot be empty or null")
    private String orgName;

    private String location;

    private Integer departmentId;

    private Integer designationId;

    @NotBlank(message = "isGap cannot be empty or null")
    private String isGap;

    @NotBlank(message = "employmentTypeCode cannot be empty or null")
    private String employmentTypeCode;

    private String gapReason;

    @NotNull(message = "experience cannot be null")
    private String experience;

    private Boolean isActive;

    private Instant fromDate;

    private Instant toDate;
}
