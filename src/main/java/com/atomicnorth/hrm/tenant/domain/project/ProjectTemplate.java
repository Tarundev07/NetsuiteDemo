package com.atomicnorth.hrm.tenant.domain.project;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "ses_m02_project_template")
public class ProjectTemplate extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TEMPLATE_ID")
    private Integer templateId;

    @Column(name = "PROJECT_TEMPLATE_NAME")
    private String projectTemplateName;

    @Column(name = "PROJECT_TYPE")
    private String projectType;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IS_ACTIVE", length = 1)
    private String isActive;

    // One-to-Many Relationship with ProjectTemplateTask
    @JsonManagedReference
    @OneToMany(mappedBy = "projectTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectTemplateTask> projectTemplateTasks;
}

