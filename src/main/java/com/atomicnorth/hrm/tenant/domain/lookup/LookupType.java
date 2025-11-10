package com.atomicnorth.hrm.tenant.domain.lookup;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "ses_m00_lookup_types")
public class LookupType implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOOKUP_ID")
    private Integer lookupId;


    @Column(name = "LOOKUP_TYPE", nullable = false, unique = true)
    private String lookupType;

    @Column(name = "MEANING")
    private String meaning;
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "APP_MODULE")
    private Long appModule;

    @Column(name = "MODULE_FUNCTION")
    private Long moduleFunction;

    @Column(name = "MEANING_SHORTCODE")
    private String meaningShortCode;

    @Column(name = "DESCRIPTION_SHORTCODE")
    private String descriptionShortCode;
    @Column(name = "ACTIVE_FLAG")
    private String activeFlag;
    @Column(name = "ACTIVE_START_DATE")
    private Date activeStartDate;
    @Column(name = "ACTIVE_END_DATE")
    private Date activeEndDate;
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

}
