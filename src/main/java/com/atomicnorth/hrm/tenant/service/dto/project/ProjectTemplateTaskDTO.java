package com.atomicnorth.hrm.tenant.service.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectTemplateTaskDTO {
    private Integer projectTemplateTaskId;
    private Integer taskId;
    private String taskName;
    private String isActive;
    private String createdBy;
    private Instant createdDate;
    private String lastUpdatedBy;
    private Instant lastUpdatedDate;
}
