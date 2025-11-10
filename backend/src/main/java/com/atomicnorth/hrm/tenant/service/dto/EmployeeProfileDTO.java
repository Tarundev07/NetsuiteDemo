package com.atomicnorth.hrm.tenant.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EmployeeProfileDTO implements Serializable {

    private String employeeNumber;
    private String fullName;
    private String workEmail;
    @JsonIgnore
    private String primaryContactCountryCode;
    @JsonIgnore
    private String primaryContactNumber;
    private LocalDate effectiveStartDate;
    private String shift;


    @JsonProperty("mobileNo")
    public String getMobileNo() {
        if (primaryContactNumber == null || primaryContactNumber.isBlank()) return "";
        if (primaryContactCountryCode != null && !primaryContactCountryCode.isBlank()) {
            return primaryContactCountryCode.trim() + " " + primaryContactNumber.trim();
        }
        return primaryContactNumber.trim();
    }
}
