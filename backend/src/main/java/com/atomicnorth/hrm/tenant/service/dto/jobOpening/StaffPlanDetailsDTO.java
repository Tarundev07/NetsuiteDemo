package com.atomicnorth.hrm.tenant.service.dto.jobOpening;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
public class StaffPlanDetailsDTO extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long staffPlanDetailsId;

    private Integer jobRequisitionId;

    private String jobRequisitionName;

    @NotNull(message = "Designation cannot be null")
    private Integer designationId;

    @NotNull(message = "Department cannot be null")
    private Integer departmentId;

    @NotNull(message = "Vacancies cannot be null")
    private Integer requiredVacancies;

    @NotNull(message = "Estimated cost cannot be null")
    private Integer estimateCost;

    private String isActive;
}
