package com.atomicnorth.hrm.tenant.service.dto.manageColumn;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SesM00UserManageColumnsResponseDTO {
    private Integer userManageColumnId;
    private Integer userId;
    private Integer moduleId;
    private Integer moduleFeatureId;
    private String pageKey;
    private Boolean isPublic;
    private Date creationDate;
    private Date lastUpdateDate;
    private Integer createdBy;
    private Integer lastUpdatedBy;
    private String operationSource;
    private String pageSection;
    private List<SesM01UserManageColumnDetailsResponseDTO> columnDetails;
}
