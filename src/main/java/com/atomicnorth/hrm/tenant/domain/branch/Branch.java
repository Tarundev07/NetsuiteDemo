package com.atomicnorth.hrm.tenant.domain.branch;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "ses_m00_branch")
public class Branch extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "IS_ACTIVE", nullable = false)
    private String isActive;

    @Column(name = "CODE", nullable = false, unique = true)
    private String code;

    @Column(name = "START_DATE", nullable = false)
    private Date startDate;

    @Column(name = "ADDRESS_ID", nullable = false)
    private Integer addressId;

}
