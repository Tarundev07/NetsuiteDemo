package com.atomicnorth.hrm.tenant.domain.attendance;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "ses_m07_attendance_moaf")
public class AttendanceMoaf extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FORM_RF_NUM")
    private Integer formRfNum;

    @Column(name = "MOAF_DATE")
    private LocalDate moafDate;

    @Column(name = "REQUEST_NUMBER")
    private String requestNumber;

    @Column(name = "DIRECTION")
    private String direction;

    @Column(name = "IN_TIME")
    private LocalDateTime inTime;

    @Column(name = "OUT_TIME")
    private LocalDateTime outTime;

    @Column(name = "EMPLOYEE_ID", length = 50)
    private Integer employeeId;

    @Column(name = "STATUS", length = 50)
    private String status;

    @Column(name = "REASON", length = 200)
    private String reason;

    @Column(name = "CATEGORY", length = 50)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", insertable = false, updatable = false)
    private Employee employee;
}