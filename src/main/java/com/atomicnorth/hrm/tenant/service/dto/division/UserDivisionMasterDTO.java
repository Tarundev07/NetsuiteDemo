package com.atomicnorth.hrm.tenant.service.dto.division;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Date;

@Data
public class UserDivisionMasterDTO {
    private Long divisionId;


    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Active flag is mandatory")
    private String activeFlag;

    @NotBlank(message = "Display name is mandatory")
    private String displayName;

    private Integer displayOrder;
    private String recordInfo;

    // Optional: Ensure activeFlag is always uppercase
    public void setActiveFlag(String activeFlag) {
        this.activeFlag = activeFlag != null ? activeFlag.toUpperCase() : null;
    }


    // Inherited from AbstractAuditingEntity
    private String cAttribute1;
    private String cAttribute2;
    private String cAttribute3;
    private String cAttribute4;
    private String cAttribute5;
    private String cAttribute6;
    private String cAttribute7;
    private String cAttribute8;
    private String cAttribute9;
    private String cAttribute10;
    private String cAttribute11;
    private String cAttribute12;
    private String cAttribute13;
    private String cAttribute14;
    private String cAttribute15;

    private Integer nAttribute16;
    private Integer nAttribute17;
    private Integer nAttribute18;
    private Integer nAttribute19;
    private Integer nAttribute20;
    private Integer nAttribute21;
    private Integer nAttribute22;
    private Integer nAttribute23;
    private Integer nAttribute24;
    private Integer nAttribute25;

    private Date dAttribute26;
    private Date dAttribute27;
    private Date dAttribute28;
    private Date dAttribute29;
    private Date dAttribute30;

    private String jAttribute31;
    private String jAttribute32;

    private String createdBy;
    private Instant createdDate;
    private String lastUpdatedBy;
    private Instant lastUpdatedDate;
}

