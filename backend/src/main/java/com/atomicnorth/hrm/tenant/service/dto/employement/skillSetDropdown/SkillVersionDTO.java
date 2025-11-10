package com.atomicnorth.hrm.tenant.service.dto.employement.skillSetDropdown;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SkillVersionDTO {

    @JsonProperty("SKILL_VERSION_ID")
    private Integer SKILL_VERSION_ID; // Corresponds to SKILL_VERSION_ID

    @JsonProperty("SKILL_ID")
    private Integer SKILL_ID; // Corresponds to SKILL_ID

    @JsonProperty("VERSION")
    private String VERSION; // Corresponds to VERSION

    @JsonProperty("IS_DELETED")
    private String IS_DELETED; // Corresponds to IS_DELETED

    @JsonProperty("ENTITY_ID")
    private Integer ENTITY_ID; // Corresponds to ENTITY_ID

    @JsonProperty("CLIENT_ID")
    private Integer CLIENT_ID; // Corresponds to CLIENT_ID

    @JsonProperty("LAST_UPDATE_SESSION_ID")
    private String LAST_UPDATE_SESSION_ID; // Corresponds to LAST_UPDATE_SESSION_ID

    // Audit fields
    @JsonProperty("CREATION_DATE")
    private LocalDateTime CREATION_DATE; // Corresponds to CREATION_DATE

    @JsonProperty("CREATED_BY")
    private String CREATED_BY; // Corresponds to CREATED_BY

    @JsonProperty("LAST_UPDATED_BY")
    private String LAST_UPDATED_BY; // Corresponds to LAST_UPDATED_BY

    @JsonProperty("LAST_UPDATED_DATE")
    private LocalDateTime LAST_UPDATED_DATE; // Corresponds to LAST_UPDATED_DATE

    @JsonProperty("RECORD_INFO")
    private String RECORD_INFO; // Corresponds to RECORD_INFO

}
