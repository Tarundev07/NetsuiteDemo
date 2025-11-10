package com.atomicnorth.hrm.tenant.service.dto.jobOpening;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
public class StaffPlanDTO extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Staff name cannot be null")
    private String staffName;

//    @NotNull(message = "Department ID cannot be null")
//    private Long departmentId;

    @NotNull(message = "Is Active cannot be null")
    private String isActive;

    @NotNull(message = "From Date can not be null")
    private Date effectiveStartDate;

    @NotNull(message = "To Date can not be null")
    private Date effectiveEndDate;

    private List<StaffPlanDetailsDTO> staffPlanDetails;

}
