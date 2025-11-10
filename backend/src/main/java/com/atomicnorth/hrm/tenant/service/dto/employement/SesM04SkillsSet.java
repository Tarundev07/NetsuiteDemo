package com.atomicnorth.hrm.tenant.service.dto.employement;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SesM04SkillsSet implements Serializable {

    private Integer skillId;
    private String name;
    private String description;
    private String categoryCode;
    private String isDeleted;
    private String isActive;
    private Integer entityId;
    private Integer clientId;
    private String lastUpdatedBy;
    private Date lastUpdateDate;
    private String createdBy;
    private Date creationDate;
    private String lastUpdateSessionId;
}
