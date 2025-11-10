package com.atomicnorth.hrm.tenant.service.dto.employement.skillSetDropdown;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SkillListDTO {

    @JsonProperty("SKILL_ID")
    private Integer SKILL_ID;

    @JsonProperty("NAME")
    private String NAME;

    @JsonProperty("DESCRIPTION")
    private String DESCRIPTION;

    @JsonProperty("CATEGORY_CODE")
    private String CATEGORY_CODE;

    @JsonProperty("IS_DELETED")
    private String IS_DELETED;

    @JsonProperty("IS_ACTIVE")
    private String IS_ACTIVE;

    @JsonProperty("ENTITY_ID")
    private Integer ENTITY_ID;

    @JsonProperty("CLIENT_ID")
    private Integer CLIENT_ID;

    @JsonProperty("LAST_UPDATE_SESSION_ID")
    private String LAST_UPDATE_SESSION_ID;

    // Audit fields
    @JsonProperty("CREATION_DATE")
    private LocalDateTime CREATION_DATE;

    @JsonProperty("CREATED_BY")
    private String CREATED_BY;

    @JsonProperty("LAST_UPDATED_BY")
    private String LAST_UPDATED_BY;

    @JsonProperty("LAST_UPDATED_DATE")
    private LocalDate LAST_UPDATED_DATE;

    @JsonProperty("RECORD_INFO")
    private String RECORD_INFO;


}
