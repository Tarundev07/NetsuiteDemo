package com.atomicnorth.hrm.tenant.domain.roles;

import com.atomicnorth.hrm.tenant.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "ses_m00_roles")
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROLE_ID")
    private Integer roleId;
    @Column(name = "ROLE_CODE")
    private String roleCode;
    @Column(name = "ROLE_NAME_CODE")
    private String roleNameCode;
    @Column(name = "ROLE_DESCRIPTION_CODE")
    private String roleDescriptionCode;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "EFFECTIVE_START_DATE")
    private Date startDate;
    @Column(name = "EFFECTIVE_END_DATE")
    private Date endDate;
    @Column(name = "MODULE_ID")
    private Integer ModuleId;
    @Column(name = "FUNCTION_ID")
    private Integer functionId;
    @Column(name = "OPEN_SOURCE")
    private String OpenSource;
    @Column(name = "ADD_ROLE_NAME")
    private String addRoleName;

    @Column(name = "SSO_ROLE_UNIQUE_KEY")
    private String sso_role_unique_key;
    @Column(name = "SSO_ROLE_ACTIVATION")
    private String sso_role_activation;

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

    @Column(name = "J_ATTRIBUTE31", columnDefinition = "TEXT")
    private String jAttribute31;

    @Column(name = "J_ATTRIBUTE32", columnDefinition = "TEXT")
    private String jAttribute32;

    @Column(name = "CREATION_DATE")
    private Date creationDate;

    @Column(name = "LAST_UPDATED_BY", length = 100)
    private String lastUpdatedBy;

    @Column(name = "LAST_UPDATED_DATE")
    private Date lastUpdatedDate;

    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;

    @Column(name = "RECORD_INFO", columnDefinition = "TEXT")
    private String recordInfo;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RolePermissions> rolePermissions;

    @Transient
    private String roleName;

    @Transient
    private Integer totalPermission;

    @Transient
    private Integer totalRecords;
}