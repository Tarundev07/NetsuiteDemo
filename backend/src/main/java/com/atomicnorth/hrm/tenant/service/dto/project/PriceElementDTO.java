package com.atomicnorth.hrm.tenant.service.dto.project;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class PriceElementDTO {
    private Long priceElementId;
    private String priceElementName;
    private Double basePrice;
    private Integer baseTime;
    private String isActive;
    private String isDeleted;
    private String lastUpdatedBy;
    private Timestamp lastUpdatedDate;
    private String createdBy;
    private String createdByUserCode;
    private Timestamp creationDate;
    private Integer mappedGroupCount;
}
