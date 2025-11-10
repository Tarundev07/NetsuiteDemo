package com.atomicnorth.hrm.tenant.service.dto.employement.employeeAddressDTO;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import java.io.Serializable;

@Data
public class Ses_M00_Addresses_Response extends AbstractAuditingEntity<Long> implements Serializable {

    private Integer addressId;
    private Integer username;
    private String addressTypeCode;
    private String addressText;
    private Integer cityId;
    private Integer countryId;
    private Integer pincode;
    private Integer stateId;
    private String isDeleted;
    private Integer entityId;
    private Integer clientId;
    private String lastUpdateSessionId;
    private String recordInfo;
}
