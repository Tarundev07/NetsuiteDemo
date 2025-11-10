package com.atomicnorth.hrm.tenant.service.dto.employement.employeeAddressDTO;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ses_M00_Addresses_Request extends AbstractAuditingEntity<Long> implements Serializable {

    private Integer addressId;
    private Integer username;
    private String addressTypeCode;
    private String addressText;
    private Integer cityId;
    private Integer countryId;
    private Integer pincode;
    private Integer stateId;
    private String isDeleted;  // Default value is "N"
    private Integer entityId;
    private Integer clientId;
    private String lastUpdateSessionId;
    private String recordInfo;

}
