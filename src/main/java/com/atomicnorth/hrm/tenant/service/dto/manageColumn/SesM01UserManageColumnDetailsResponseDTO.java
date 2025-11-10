package com.atomicnorth.hrm.tenant.service.dto.manageColumn;

import lombok.Data;

import java.util.Date;

@Data
public class SesM01UserManageColumnDetailsResponseDTO {
    private Integer userManageColumnDetailsId;
    private String columnLookupCode;
    private String columnLookupCodeHeader;
    private Integer displaySno;
    private Date creationDate;
    private Date lastUpdateDate;
    private Integer createdBy;
    private Integer lastUpdatedBy;
    private String operationSource;
    private String isLocked;
}
