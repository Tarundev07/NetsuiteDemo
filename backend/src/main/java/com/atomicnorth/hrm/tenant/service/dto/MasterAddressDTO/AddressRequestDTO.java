package com.atomicnorth.hrm.tenant.service.dto.MasterAddressDTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AddressRequestDTO {
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
    private String createdBy;
    private String lastUpdatedBy;
}


