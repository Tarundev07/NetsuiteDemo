package com.atomicnorth.hrm.tenant.domain.project;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ses_m02_project_template_task")
public class ProjectTemplateTask extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROJECT_TEMPLATE_TASK_ID")
    private Integer projectTemplateTaskId;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private ProjectTemplate projectTemplate;

    @Column(name = "TASK_ID", unique = true)
    private Integer taskId;

    @Column(name = "IS_ACTIVE", length = 1)
    private String isActive;
}

