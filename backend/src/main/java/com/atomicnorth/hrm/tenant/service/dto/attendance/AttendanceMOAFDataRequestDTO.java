package com.atomicnorth.hrm.tenant.service.dto.attendance;

import lombok.Data;

import java.io.Serializable;

@Data
public class AttendanceMOAFDataRequestDTO implements Serializable {

    private String[] moafDateArr;
    private String projectId;
    private String username;
    private String reason;
    private String moafCategory;


}
