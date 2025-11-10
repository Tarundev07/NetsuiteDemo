package com.atomicnorth.hrm.tenant.domain.designation;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "ses_m00_designation")
public class Designation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DESIGNATION_ID")
    private Integer id;

    @Column(name = "DESIGNATION_NAME", nullable = false, length = 50)
    private String designationName;

    @Column(name = "DESCRIPTION", length = 150)
    private String description;

    @Column(name = "LEVEL_MASTER_ID")
    private Integer levelMasterId;

    @Column(name = "APPRAISAL_TEMPLATE_ID")
    private Integer appraisalTemplateId;

    @Column(name = "START_DATE")
    private Date startDate;

    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "STATUS", length = 50)
    private String status;
    @Column(name = "CREATED_BY")
    private String createdBy;
    @Column(name = "CREATION_DATE")
    private Date createdDate;
    @Column(name = "LAST_UPDATED_BY", length = 50)
    private String lastModifiedBy;
    @Column(name = "LAST_UPDATED_DATE")
    private Date lastModifiedDate;
    @Column(name = "RECORD_INFO", columnDefinition = "TEXT")
    private String recordInfo;

    @JsonManagedReference
    @OneToMany(mappedBy = "designation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DesignationSkill> skills = new ArrayList<>();

    @Column(name = "C_ATTRIBUTE1", length = 240)
    private String cAttribute1;

    @Column(name = "C_ATTRIBUTE2", length = 240)
    private String cAttribute2;

    @Column(name = "C_ATTRIBUTE3", length = 240)
    private String cAttribute3;

    @Column(name = "C_ATTRIBUTE4", length = 240)
    private String cAttribute4;

    @Column(name = "C_ATTRIBUTE5", length = 240)
    private String cAttribute5;

    @Column(name = "C_ATTRIBUTE6", length = 240)
    private String cAttribute6;

    @Column(name = "C_ATTRIBUTE7", length = 240)
    private String cAttribute7;

    @Column(name = "C_ATTRIBUTE8", length = 240)
    private String cAttribute8;

    @Column(name = "C_ATTRIBUTE9", length = 240)
    private String cAttribute9;

    @Column(name = "C_ATTRIBUTE10", length = 240)
    private String cAttribute10;

    @Column(name = "C_ATTRIBUTE11", length = 240)
    private String cAttribute11;

    @Column(name = "C_ATTRIBUTE12", length = 240)
    private String cAttribute12;

    @Column(name = "C_ATTRIBUTE13", length = 240)
    private String cAttribute13;

    @Column(name = "C_ATTRIBUTE14", length = 240)
    private String cAttribute14;

    @Column(name = "C_ATTRIBUTE15", length = 240)
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
    @Temporal(TemporalType.TIMESTAMP)
    private Date dAttribute26;

    @Column(name = "D_ATTRIBUTE27")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dAttribute27;

    @Column(name = "D_ATTRIBUTE28")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dAttribute28;

    @Column(name = "D_ATTRIBUTE29")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dAttribute29;

    @Column(name = "D_ATTRIBUTE30")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dAttribute30;

    @Column(name = "J_ATTRIBUTE31", length = 50)
    private String jAttribute31;

    @Column(name = "J_ATTRIBUTE32", length = 50)
    private String jAttribute32;

}

