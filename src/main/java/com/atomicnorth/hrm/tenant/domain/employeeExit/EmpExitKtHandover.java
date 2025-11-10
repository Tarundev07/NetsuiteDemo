package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "emp_exit_kt_handover")
public class EmpExitKtHandover extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "KT_HANDOVER_ID")
    private Integer id;

    @Column(name = "EXIT_REQUEST_ID")
    private Integer exitRequestId;

    @Column(name = "HANDOVER_START_DATE")
    private LocalDate handoverStartDate;

    @Column(name = "HANDOVER_END_DATE")
    private LocalDate handoverEndDate;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "REMARKS")
    private String remarks;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXIT_REQUEST_ID", insertable = false, updatable = false)
    @JsonBackReference
    private EmpExitRequest exitRequest;

    @OneToMany(mappedBy = "ktHandover", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<EmpExitKtHandoverDetail> details;
}
