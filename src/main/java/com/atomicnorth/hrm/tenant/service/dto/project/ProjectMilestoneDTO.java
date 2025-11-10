package com.atomicnorth.hrm.tenant.service.dto.project;

import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
public class ProjectMilestoneDTO {

    private Integer projectMilestoneId;

    private String milestoneName;

    private Integer projectRfNum;

    private Integer projectMilestoneTypeId;

    private String projectMilestoneTypeName;

    private Integer sequenceOrder;

    private Double amount;

    private Date startDate;

    private Date endDate;

    private String remark;

    private String isActive;

    private String isDeleted;

    private String lastUpdatedBy;

    private Instant lastUpdateDate;

    private String createdBy;

    private Instant creationDate;

    private String createdByName;

}
