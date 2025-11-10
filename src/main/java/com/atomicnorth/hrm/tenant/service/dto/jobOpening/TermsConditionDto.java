package com.atomicnorth.hrm.tenant.service.dto.jobOpening;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
public class TermsConditionDto extends AbstractAuditingEntity<Integer> {
    private Integer termsConditionId;
    @NotNull(message = "Title cannot be blank.")
    @Size(max = 100, message = "Title must not exceed 100 characters.")
    private String title;
    @NotNull(message = "Description cannot be blank.")
    @Size(max = 100, message = "Description must not exceed 300 characters.")
    private String description;
    @NotNull(message = "Start Date cannot be blank.")
    private Date startDate;
    private Date endDate;
    private String flag;
    private String fullName;
    @NotBlank(message = "Status cannot be blank.")
    private String isActive;
    @NotNull(message = "Job Terms  cannot be blank.")
    private List<TermsConditionDepartmentDTO> termsConditionDepartmentDTO;

}
