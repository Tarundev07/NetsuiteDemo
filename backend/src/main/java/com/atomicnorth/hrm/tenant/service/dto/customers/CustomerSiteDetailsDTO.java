package com.atomicnorth.hrm.tenant.service.dto.customers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerSiteDetailsDTO {
    private Integer siteId;
    private String siteCode;
    private String siteName;
    private Integer accountId;
    private String accountCode;
    private String accountName;
    private Integer customerId;
    private String customerName;
}
