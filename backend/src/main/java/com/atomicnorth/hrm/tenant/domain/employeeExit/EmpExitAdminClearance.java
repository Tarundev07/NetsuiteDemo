package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "emp_exit_admin_clearance")
public class EmpExitAdminClearance extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ADMIN_CLEARANCE_ID")
    private Integer id;

    @Column(name = "EXIT_REQUEST_ID")
    private Integer exitRequestId;

    @Column(name = "ITEM")
    private String item;

    @Column(name = "CLEARANCE_STATUS")
    private String clearanceStatus;

    @Column(name = "CLEARED_BY")
    private Integer clearedBy;

    @Column(name = "CLEARED_DATE")
    private LocalDate clearedDate;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "ATTACHMENT")
    private String attachment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXIT_REQUEST_ID", insertable = false, updatable = false)
    @JsonBackReference
    private EmpExitRequest exitRequest;
}
