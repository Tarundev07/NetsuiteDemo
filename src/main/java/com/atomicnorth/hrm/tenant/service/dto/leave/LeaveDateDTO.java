package com.atomicnorth.hrm.tenant.service.dto.leave;

import lombok.Data;

import java.io.Serializable;

@Data
public class LeaveDateDTO implements Serializable {

    private String leaveHead;
    private String startDate;
    private String endDate;
}
