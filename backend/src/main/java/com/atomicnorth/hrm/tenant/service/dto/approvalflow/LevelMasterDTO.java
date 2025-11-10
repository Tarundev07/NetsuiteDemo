package com.atomicnorth.hrm.tenant.service.dto.approvalflow;

import lombok.Data;


@Data
public class LevelMasterDTO {
    private Integer levelId;
    private String levelName;
    private String levelCode;
    private Integer orderBy;
    private String isManager;
    private String isHr;
    private String isActive;
}
