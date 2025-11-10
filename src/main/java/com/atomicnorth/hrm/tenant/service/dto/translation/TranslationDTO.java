package com.atomicnorth.hrm.tenant.service.dto.translation;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TranslationDTO implements Serializable {


    private Long translationId;
    private Integer moduleId;
    private Integer functionId;

    private Integer languageId;
    private String shortCode;
    private String description;
    private String status;
    private LocalDateTime effectiveStartDate;
    private LocalDateTime effectiveEndDate;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}