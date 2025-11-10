package com.atomicnorth.hrm.tenant.domain.approvalflow;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
@Data
@Entity(name="organization_level_master")
public class Level extends AbstractAuditingEntity<T> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="LEVEL_ID")
    private Integer levelId;
    @Column(name="LEVEL_NAME")
    private String levelName;
    @Column(name="LEVEL_CODE")
    private String levelCode;
    @Column(name="ORDER_BY")
    private Integer orderBy;
    @Column(name = "IS_MANAGER")
    private String isManager;
    @Column(name = "IS_HR")
    private String isHr;
    @Column(name="IS_ACTIVE")
    private String isActive;
}