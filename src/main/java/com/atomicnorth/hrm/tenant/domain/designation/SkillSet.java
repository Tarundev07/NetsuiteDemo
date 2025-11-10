package com.atomicnorth.hrm.tenant.domain.designation;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ses_m00_skills_set")
public class SkillSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SKILL_ID")
    private Integer skillId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CATEGORY_CODE")
    private String categoryCode;

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

    @Column(name = "C_ATTRIBUTE1")
    private String cAttribute1;

    @Column(name = "C_ATTRIBUTE2")
    private String cAttribute2;

    @Column(name = "C_ATTRIBUTE3")
    private String cAttribute3;

    @Column(name = "C_ATTRIBUTE4")
    private String cAttribute4;

    @Column(name = "C_ATTRIBUTE5")
    private String cAttribute5;

    @Column(name = "C_ATTRIBUTE6")
    private String cAttribute6;

    @Column(name = "C_ATTRIBUTE7")
    private String cAttribute7;

    @Column(name = "C_ATTRIBUTE8")
    private String cAttribute8;

    @Column(name = "C_ATTRIBUTE9")
    private String cAttribute9;

    @Column(name = "C_ATTRIBUTE10")
    private String cAttribute10;

    @Column(name = "C_ATTRIBUTE11")
    private String cAttribute11;

    @Column(name = "C_ATTRIBUTE12")
    private String cAttribute12;

    @Column(name = "C_ATTRIBUTE13")
    private String cAttribute13;

    @Column(name = "C_ATTRIBUTE14")
    private String cAttribute14;

    @Column(name = "C_ATTRIBUTE15")
    private String cAttribute15;

    @Column(name = "N_ATTRIBUTE16")
    private Integer nAttribute16;

    @Column(name = "N_ATTRIBUTE17")
    private Integer nAttribute17;

    @Column(name = "N_ATTRIBUTE18")
    private Integer nAttribute18;

    @Column(name = "N_ATTRIBUTE19")
    private Integer nAttribute19;

    @Column(name = "N_ATTRIBUTE20")
    private Integer nAttribute20;

    @Column(name = "N_ATTRIBUTE21")
    private Integer nAttribute21;

    @Column(name = "N_ATTRIBUTE22")
    private Integer nAttribute22;

    @Column(name = "N_ATTRIBUTE23")
    private Integer nAttribute23;

    @Column(name = "N_ATTRIBUTE24")
    private Integer nAttribute24;

    @Column(name = "N_ATTRIBUTE25")
    private Integer nAttribute25;

    @Column(name = "D_ATTRIBUTE26")
    private LocalDateTime dAttribute26;

    @Column(name = "D_ATTRIBUTE27")
    private LocalDateTime dAttribute27;

    @Column(name = "D_ATTRIBUTE28")
    private LocalDateTime dAttribute28;

    @Column(name = "D_ATTRIBUTE29")
    private LocalDateTime dAttribute29;

    @Column(name = "D_ATTRIBUTE30")
    private LocalDateTime dAttribute30;

    @Column(name = "J_ATTRIBUTE31")
    private String jAttribute31;

    @Column(name = "J_ATTRIBUTE32")
    private String jAttribute32;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @Column(name = "LAST_UPDATED_DATE")
    private LocalDateTime lastUpdatedDate;

    @Column(name = "RECORD_INFO")
    private String recordInfo;
}