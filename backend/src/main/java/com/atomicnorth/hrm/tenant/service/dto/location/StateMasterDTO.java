package com.atomicnorth.hrm.tenant.service.dto.location;

import lombok.Data;

@Data
public class StateMasterDTO {
    private Integer stateId;
    private Integer countryId;
    private String stateName;
    private String timeZone;
    private String isActive;
}
