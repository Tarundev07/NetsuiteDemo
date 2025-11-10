package com.atomicnorth.hrm.tenant.service.dto.project;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ProjectPricingDTO {
    private String taskName;
    private String taskStatus;
    private String taskDescription;
    private String priceElementName;
    private BigDecimal overridePrice;
    private BigDecimal overrideTime;
}
