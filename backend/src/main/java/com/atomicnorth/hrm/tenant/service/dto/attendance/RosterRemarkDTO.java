package com.atomicnorth.hrm.tenant.service.dto.attendance;

import lombok.Data;

import java.util.Date;

@Data
public class RosterRemarkDTO {
    private Integer rosterRemarkId;
    private Date date;
    private Integer username;
    private Integer lastUpdateSessionId;
    private String shiftId;
    private String assigneeRemark;
    private String tempLeaveFlag;
    private Integer entityId;
    private Integer clientId;
    private String lastUpdatedBy;
    private Date lastUpdateDate;
    private String createdBy;
    private Date creationDate;

}
