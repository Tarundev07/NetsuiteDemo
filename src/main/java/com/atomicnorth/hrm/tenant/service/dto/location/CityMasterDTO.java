package com.atomicnorth.hrm.tenant.service.dto.location;

import lombok.Data;

@Data
public class CityMasterDTO {
    private Integer cityId;
    private Integer stateId;
    private String cityName;
    private String timeZone;
    private String isActive;
}
