package com.atomicnorth.hrm.tenant.domain.accessgroup;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "ses_m00_user_division_master")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SesM00UserDivisionMaster extends AbstractAuditingEntity<Long> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DIVISION_ID", nullable = false)
    private Long divisionId;

    @Column(name = "NAME", nullable = false, length = 100, columnDefinition = "VARCHAR(100) COLLATE utf8mb3_bin")
    private String name;

    @Column(name = "DISPLAY_ORDER", nullable = false)
    private Integer displayOrder;

    @Column(name = "ACTIVE_FLAG", nullable = false, length = 1, columnDefinition = "VARCHAR(1) COLLATE utf8mb3_bin")
    private String activeFlag;

    @Column(name = "DISPLAY_NAME", nullable = false, length = 100, columnDefinition = "VARCHAR(100) COLLATE utf8mb3_bin DEFAULT 'TEXT'")
    private String displayName;


    @Column(name = "C_ATTRIBUTE1", length = 240)
    private String cAttribute1;
    
}


