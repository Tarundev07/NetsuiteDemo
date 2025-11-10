package com.atomicnorth.hrm.tenant.service.dto.employement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeContactInfoDTO {
    private String workEmail;
    private String personalEmail;
    private String primaryContactNumber;
    private String primaryContactCountryCode;
    private String secondaryContactNumber;
    private String secondaryContactCountryCode;
    private String primaryEmergencyContactName;
    private String primaryEmergencyContactRelation;
    private String primaryEmergencyContactNumber;
    private String primaryEmergencyContactCountryCode;
    private String secondaryEmergencyContactName;
    private String secondaryEmergencyContactRelation;
    private String secondaryEmergencyContactNumber;
    private String secondaryEmergencyContactCountryCode;
    private String websiteUrl;
    private String linkedinUrl;
}

