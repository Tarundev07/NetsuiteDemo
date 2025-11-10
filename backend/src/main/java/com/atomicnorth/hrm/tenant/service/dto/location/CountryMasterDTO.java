package com.atomicnorth.hrm.tenant.service.dto.location;

import lombok.Data;

@Data
public class CountryMasterDTO {

    private Integer countryId;
    private String countryName;
    private String phoneCode;
    private String region;
    private String isActive;

}
