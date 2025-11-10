package com.atomicnorth.hrm.tenant.service.dto.accessgroup;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserWatchersDTO implements Serializable {

    private String objectCode;
    private String watcherOperation;
    private String levelCode;
    private Integer userWatcherId;
    private String watcherCode;
    private Integer[] username;
    private String objectId;
    private Integer level1;
    private Integer level2;
    private Integer level3;
    private Integer level4;
    private Integer level5;
    private Integer level6;
    private Integer level7;
    private Integer level8;
    private Integer level9;
    private Integer level10;
    private String watcherLevelCode;
    private String isPrivate;
    private String isDeleted;
    private Integer entityId;
    private Integer clientId;
    private String lastUpdatedBy;
    private Date lastUpdateDate;
    private String createdBy;
    private Date creationDate;
    private Integer lastUpdateSessionId;

    private Integer watcherLength;


}
