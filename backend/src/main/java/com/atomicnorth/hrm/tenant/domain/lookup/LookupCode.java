package com.atomicnorth.hrm.tenant.domain.lookup;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "ses_m00_lookup_codes")
public class LookupCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOOKUP_CODE_ID")
    private Integer lookupCodeId;

    @Column(name = "LOOKUP_TYPE")
    private String lookupType;

    @Column(name = "LOOKUP_CODE")
    private String lookupCode;
    @Column(name = "MEANING")
    private String meaning;
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "MEANING_SHORTCODE")
    private String meaningShortCode;

    @Column(name = "DESCRIPTION_SHORTCODE")
    private String descrtiptionShortCode;

    @Column(name = "LOOKUP_ID")
    private Integer lookupId;

    @Column(name = "APP_MODULE")
    private Long appModule;

    @Column(name = "MODULE_FUNCTION")
    private Long moduleFunction;
    @Column(name = "ACTIVE_FLAG")
    private String activeFlag;
    @Column(name = "ACTIVE_START_DATE")
    private Date activeStartDate;
    @Column(name = "ACTIVE_END_DATE")
    private Date activeEndDate;
    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    @Column(name = "ATTRIBUTE1")
    private String attribute1;
    @Column(name = "ATTRIBUTE2")
    private String attribute2;
    @Column(name = "ATTRIBUTE3")
    private String attribute3;
    @Column(name = "ATTRIBUTE4")
    private String attribute4;
    @Column(name = "ATTRIBUTE5")
    private String attribute5;

    @Column(name = "ATTRIBUTE6")
    private String attribute6;

    @Column(name = "ATTRIBUTE7")
    private String attribute7;

    @Column(name = "ATTRIBUTE8")
    private String attribute8;

    @Column(name = "ATTRIBUTE9")
    private String attribute9;
    @Column(name = "ATTRIBUTE10")
    private String attribute10;
    @Column(name = "ATTRIBUTE11")
    private String attribute11;
    @Column(name = "ATTRIBUTE12")
    private String attribute12;
    @Column(name = "ATTRIBUTE13")
    private String attribute13;
    @Column(name = "ATTRIBUTE14")
    private String attribute14;
    @Column(name = "ATTRIBUTE15")
    private String attribute15;
    @Column(name = "ENTITY_ID")
    private Integer entityId;
    @Column(name = "CLIENT_ID")
    private Integer clientId;
    @Column(name = "LAST_UPDATED_BY")
    private String LastUpdatedBy;
    @Column(name = "LAST_UPDATED_DATE")
    private Date lastUpdateDate;
    @Column(name = "CREATED_BY")
    private String createdBy;
    @Column(name = "CREATION_DATE")
    private Date creationDate;
    @Column(name = "LAST_UPDATE_SESSION_ID")
    private Integer lastUpdateSessionId;

    @Transient
    @JsonProperty
    private String functionName;

    @Transient
    @JsonProperty
    private String moduleName;

}
