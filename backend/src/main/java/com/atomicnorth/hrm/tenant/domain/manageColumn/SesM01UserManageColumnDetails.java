package com.atomicnorth.hrm.tenant.domain.manageColumn;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "ses_m01_user_manage_column_details")
public class SesM01UserManageColumnDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_MANAGE_COLUMN_DETAILS_ID")
    private Integer userManageColumnDetailsId;

    @ManyToOne
    @JoinColumn(name = "USER_MANAGE_COLUMN_ID", nullable = false)
    @JsonBackReference
    private SesM00UserManageColumns userManageColumn;

    @Column(name = "COLUMN_LOOKUP_CODE", nullable = false, length = 200)
    private String columnLookupCode;

    @Column(name = "DISPLAY_SNO", nullable = false)
    private Integer displaySno;

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

    @Column(name = "IS_LOCKED", nullable = false, length = 240)
    private String isLocked;
}