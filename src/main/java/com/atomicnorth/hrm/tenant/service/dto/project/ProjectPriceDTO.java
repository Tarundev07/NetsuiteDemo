package com.atomicnorth.hrm.tenant.service.dto.project;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
public class ProjectPriceDTO extends AbstractAuditingEntity {

    private Integer priceGroupProjectMappingId;
    @NotBlank(message = "priceGroupId cannot be blank")
    private String priceGroupId;

    @NotBlank(message = "projectId cannot be blank")
    private String projectId;
    @Pattern(regexp = "^(Y|N)$", message = "isDeleted must be either 'Y' or 'N'")
    private String isDeleted;
    @NotNull(message = "startDate is required")
    private Date startDate;
    @NotNull(message = "endDate is required")
    private Date endDate;

    private Integer clientId;
    private Integer lastUpdatedSessionId;
}
