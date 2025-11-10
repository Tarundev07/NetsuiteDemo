package com.atomicnorth.hrm.tenant.service.dto.employement;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SesM04SkillsVersionMaster implements Serializable {

    private Integer skillVersionId;
    private Integer skillId;
    private String version;
    private String isDeleted;
    private Integer entityId;
    private Integer clientId;
    private String lastUpdatedBy;
    private Date lastUpdateDate;
    private String createdBy;
    private Date creationDate;
    private String lastUpdateSessionId;
}
