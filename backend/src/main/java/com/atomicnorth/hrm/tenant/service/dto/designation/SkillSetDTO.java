package com.atomicnorth.hrm.tenant.service.dto.designation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillSetDTO {

    private Integer skillId;
    private String name;
    private String description;
    private String categoryCode;
    private String isDeleted;
    private String isActive;
    private Integer entityId;
    private Integer clientId;
    private String lastUpdateSessionId;
    private LocalDateTime creationDate;
    private String createdBy;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedDate;
    private String recordInfo;

    public SkillSetDTO(Integer skillId, String name, String description, String isActive, String categoryCode) {
        this.skillId = skillId;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.categoryCode = categoryCode;
    }
}
