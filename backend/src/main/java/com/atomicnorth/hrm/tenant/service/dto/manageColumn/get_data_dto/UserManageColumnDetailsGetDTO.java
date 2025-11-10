package com.atomicnorth.hrm.tenant.service.dto.manageColumn.get_data_dto;


import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserManageColumnDetailsGetDTO {
    private Integer userManageColumnDetailsId;
    private String columnLookupCode;
    private Integer displaySno;
    private Date creationDate;
    private Date lastUpdateDate;
    private Integer createdBy;
    private Integer lastUpdatedBy;
    private String operationSource;
    private String isLocked;
}

