package com.atomicnorth.hrm.tenant.service.dto.project;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
public class ProjectTemplateDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer templateId;
    private String projectTemplateName;
    private String projectType;
    private String description;
    private String isActive;
    private String createdBy;
    private Instant createdDate;
    private String lastUpdatedBy;
    private Instant lastUpdatedDate;
    //@JsonBackReference
    private List<ProjectTemplateTaskDTO> projectTemplateTasks;


}
