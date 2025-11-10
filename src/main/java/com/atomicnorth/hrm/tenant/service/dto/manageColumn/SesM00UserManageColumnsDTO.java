package com.atomicnorth.hrm.tenant.service.dto.manageColumn;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class SesM00UserManageColumnsDTO {

    private Integer userManageColumnId;

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Module ID is required")
    private Integer moduleId;

    @NotNull(message = "Module Feature ID is required")
    private Integer moduleFeatureId;

    @NotBlank(message = "Page Key is required")
    private String pageKey;

    @NotNull(message = "Public flag is required")
    private Boolean isPublic;

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

    @NotBlank(message = "Page Section is required")
    private String pageSection;

    @NotNull(message = "Column Details are required")
    private List<@Valid SesM01UserManageColumnDetailsDTO> columnDetails;

    // Additional attributes...

    // Getters and Setters
}
