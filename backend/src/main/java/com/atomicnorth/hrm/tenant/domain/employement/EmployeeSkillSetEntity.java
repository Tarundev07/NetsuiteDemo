package com.atomicnorth.hrm.tenant.domain.employement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ses_m25_employee_skill_set")
public class EmployeeSkillSetEntity extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_SKILL_ID")
    private Integer employeeSkillId;

    @Column(name = "SKILL_ID")
    private Integer skillId;

    @Column(name = "SKILL_VERSION_ID")
    private String skillVersionId;

    @Column(name = "USERNAME")
    private Integer username;

    @Column(name = "LAST_USED_DATE")
    private Date lastUsedDate;

    @Column(name = "EXPERIENCE_IN_MONTH")
    private Integer experienceInMonths;

    @Column(name = "SKILL_PROFICIENCY_CODE")
    private String skillProficiencyCode;

    @Column(name = "SKILL_CATEGORY_NAME")
    private String skillCategoryName;

    @Column(name = "SKILL_CATEGORY_CODE")
    private String skillCategoryCode;

    @Column(name = "IS_DELETED")
    private String isDeleted;

    @Column(name = "IS_ACTIVE")
    private String isActive;

    @Column(name = "ENTITY_ID")
    private Integer entityId;

    @Column(name = "CLIENT_ID")
    private Integer clientId;
    @Column(name = "LAST_UPDATE_SESSION_ID")
    private String lastUpdateSessionId;

}
