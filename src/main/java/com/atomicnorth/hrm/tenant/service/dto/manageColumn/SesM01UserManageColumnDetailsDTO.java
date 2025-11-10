package com.atomicnorth.hrm.tenant.service.dto.manageColumn;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class SesM01UserManageColumnDetailsDTO {
    private Integer userManageColumnDetailsId;

    @NotBlank(message = "Column Lookup Code is required")
    private String columnLookupCode;

    @NotNull(message = "Display SNO is required")
    private Integer displaySno;

    @NotNull(message = "Creation Date is required")
    private Date creationDate;

    @NotNull(message = "Last Update Date is required")
    private Date lastUpdateDate;

    @NotNull(message = "Created By is required")
    private Integer createdBy;

    @NotNull(message = "Last Updated By is required")
    private Integer lastUpdatedBy;

    @NotBlank(message = "Operation Source is required")
    private String operationSource;

    @NotBlank(message = "Is Locked flag is required")
    private String isLocked;
    // Additional attributes...

    // Getters and Setters
}
