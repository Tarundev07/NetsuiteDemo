package com.atomicnorth.hrm.tenant.service.dto.employement.skillSetDropdown;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Skill_Category_DTO {

    @JsonProperty("LOOKUP_CODE_ID")
    private Integer LOOKUP_CODE_ID;

    @JsonProperty("LOOKUP_TYPE")
    private String LOOKUP_TYPE;

    @JsonProperty("LOOKUP_CODE")
    private String LOOKUP_CODE;

    @JsonProperty("MEANING")
    private String MEANING;

    @JsonProperty("DESCRIPTION")
    private String DESCRIPTION;

    @JsonProperty("MEANING_SHORTCODE")
    private String MEANING_SHORTCODE;

    @JsonProperty("DESCRIPTION_SHORTCODE")
    private String DESCRIPTION_SHORTCODE;

    @JsonProperty("LOOKUP_ID")
    private String LOOKUP_ID;

    @JsonProperty("APP_MODULE")
    private String APP_MODULE;

    @JsonProperty("MODULE_FUNCTION")
    private String MODULE_FUNCTION;

    @JsonProperty("ACTIVE_FLAG")
    private String ACTIVE_FLAG;

    @JsonProperty("ACTIVE_START_DATE")
    private LocalDate ACTIVE_START_DATE;

    @JsonProperty("ACTIVE_END_DATE")
    private LocalDate ACTIVE_END_DATE;

    @JsonProperty("DISPLAY_ORDER")
    private Integer DISPLAY_ORDER;

}
