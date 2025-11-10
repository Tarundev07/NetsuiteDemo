package com.atomicnorth.hrm.tenant.service.dto.project;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectPriceMappingDTO {
    private Integer priceGroupProjectMappingId;
    private String projectName;
    private String priceGroupName;
    private String priceGroupId;
    private String projectId;
    private Date startDate;
    private Date endDate;
    private String lastUpdatedBy;
    private Date lastUpdatedDate;
    private String createdBy;
    private Date creationDate;
}
