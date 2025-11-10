package com.atomicnorth.hrm.tenant.service.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FetchUserShiftAssignHistoryReturnDataDTO implements Serializable {

    private Integer shiftEmpId;
    private Integer shiftId;
    private String shift;
    private String calendarName;
    private Integer username;
    private String fullName;
    private String userCode;
    private Date shiftStartDate;
    private Date shiftEndDate;
    private String isActive;
    private String lastUpdatedBy;
    private Date lastUpdateDate;
}
