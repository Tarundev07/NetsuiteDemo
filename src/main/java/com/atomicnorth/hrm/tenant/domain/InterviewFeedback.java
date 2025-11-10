package com.atomicnorth.hrm.tenant.domain;

import com.atomicnorth.hrm.util.JsonConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "ses_m05_interview_feedback")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class InterviewFeedback implements Serializable {

    @Id
    @Column(name = "INTERVIEW_FEEDBACK_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewFeedbackId;
    @Column(name = "INTERVIEW_ID")
    private Long interviewId;
    @Column(name = "JOB_APPLICANT_ID", nullable = false)
    private Integer jobApplicantId;
    @Column(name = "INTERVIEW_RESULT_CODE")
    private String interviewResultCode;
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;
    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    private LocalDate createdDate;
    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;
    @Column(name = "UPDATED_DATE")
    private LocalDate updatedDate;
    @Column(name = "UPDATED_BY")
    private String updatedBy;
    @Convert(converter = JsonConverter.class)
    @Column(name = "SKILL_FEEDBACK", columnDefinition = "json")
    private Map<String, Integer> skillFeedback;
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
    private LocalDateTime dAttribute26;
    @Column(name = "D_ATTRIBUTE27")
    private LocalDateTime dAttribute27;
    @Column(name = "D_ATTRIBUTE28")
    private LocalDateTime dAttribute28;
    @Column(name = "D_ATTRIBUTE29")
    private LocalDateTime dAttribute29;
    @Column(name = "D_ATTRIBUTE30")
    private LocalDateTime dAttribute30;
    @Column(name = "J_ATTRIBUTE31", length = 50)
    private String jAttribute31;
    @Column(name = "J_ATTRIBUTE32", length = 50)
    private String jAttribute32;
}
