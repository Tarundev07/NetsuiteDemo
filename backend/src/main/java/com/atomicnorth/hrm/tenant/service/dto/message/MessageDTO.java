package com.atomicnorth.hrm.tenant.service.dto.message;

import lombok.Data;

@Data
public class MessageDTO {
    private Long messageId;
    private Integer moduleId;
    private Integer moduleFunctionId;
    private Integer featureId;
    private String messageCode;
    private String messageDescriptionCode;
    private String messageTextCode;
    private String messageMeaning;
    private String messageDescription;
    private String messageType;
    private String messageSeverity;
    private String status;
}
