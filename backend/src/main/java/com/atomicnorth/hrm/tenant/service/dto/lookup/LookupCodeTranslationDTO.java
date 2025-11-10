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
public class LookupCodeTranslationDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private int lookupCodeId;
    private String lookupType;
    private String lookupCode;
    private String lookupId;
    private String appModuleId;
    private String moduleFunction;
    private String meaning;
    private String description;
    private String meaningShortCode;
    private String descriptionShortCode;
    private String activeFlag;
    private Date activeStartDate;
    private Date activeEndDate;
    private Integer displayOrder;
    private String attribute1;
    private String attribute2;
    private String attribute3;
    private String attribute4;
    private String attribute5;
    private String attribute6;
    private String attribute7;
    private String attribute8;
    private String attribute9;
    private String attribute10;
    private String attribute11;
    private String attribute12;
    private String attribute13;
    private String attribute14;
    private String attribute15;
    private int entityId;
    private int clientId;
    private String lastUpdatedBy;
    private Date lastUpdateDate;
    private String createdBy;
    private Date creationDate;
    private int lastUpdateSessionId;

}
