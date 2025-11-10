package com.atomicnorth.hrm.tenant.domain.manageColumn;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ses_m00_user_manage_columns")
public class SesM00UserManageColumns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_MANAGE_COLUMN_ID")
    private Integer userManageColumnId;

    @Column(name = "USER_ID", nullable = false)
    private Integer userId;

    @Column(name = "MODULE_ID")
    private Integer moduleId;

    @Column(name = "MODULE_FEATURE_ID")
    private Integer moduleFeatureId;

    @Column(name = "PAGE_KEY", nullable = false, length = 100)
    private String pageKey;

    @Column(name = "IS_PUBLIC")
    private Boolean isPublic;

    @Column(name = "CREATION_DATE", nullable = false)
    private Date creationDate;

    @Column(name = "LAST_UPDATE_DATE", nullable = false)
    private Date lastUpdateDate;

    @Column(name = "CREATED_BY", nullable = false)
    private Integer createdBy;

    @Column(name = "LAST_UPDATED_BY", nullable = false)
    private Integer lastUpdatedBy;

    @Column(name = "OPERATION_SOURCE", nullable = false, length = 50)
    private String operationSource;

    @Column(name = "PAGE_SECTION", length = 255)
    private String pageSection;

    @OneToMany(mappedBy = "userManageColumn", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SesM01UserManageColumnDetails> columnDetails;
}