package com.atomicnorth.hrm.tenant.service.dto.lookup;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LookupTypeTranslationDTO implements Serializable {

    private Integer lookupId;
    private String lookupType;
    private String appModule;
    private String moduleFunction;
    private String meaning;
    private String description;
    private String shortCode;
    private String meaningCode;
    private String activeFlag;
    private Date activeStartDate;
    private Date activeEndDate;
    private String attribute1;
    private String attribute2;
    private String attribute3;
    private String attribute4;
    private String attribute5;
    private Integer entityId;
    private Integer clientId;
    private String lastUpdatedBy;
    private Date lastUpdateDate;
    private String createdBy;
    private Date creationDate;
    private Integer lastUpdateSessionId;


    // Constructor

    // Constructor, getters, and setters

}
