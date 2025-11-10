package com.atomicnorth.hrm.tenant.service.dto.MasterAddressDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AddressResponseDTO {
    private Integer addressId;
    private Integer branchId;
    private String addressText1;
    private String addressText2;
    private String landmark;
    private String nearestLocation;
    private Double latitude;
    private Double longitude;
    private Boolean isPrimary;
    private String addressCategory;
    private String directionInstructions;
    private String city;
    private String state;
    private String country;
    private Integer pincode;
    private Boolean isDeleted;
    private LocalDateTime creationDate;
    private String createdBy;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedDate;
}

