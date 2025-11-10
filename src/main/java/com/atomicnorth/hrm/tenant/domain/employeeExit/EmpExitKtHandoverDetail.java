package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "emp_exit_kt_handover_detail")
public class EmpExitKtHandoverDetail extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "KT_DETAIL_ID")
    private Integer id;

    @Column(name = "KT_HANDOVER_ID")
    private Integer ktHandoverId;

    @Column(name = "PROJECT_ID")
    private Integer projectId;

    @Column(name = "KT_TO_EMPLOYEE_ID")
    private Integer ktToEmployeeId;

    @Column(name = "KT_DATE")
    private LocalDate ktDate;

    @Column(name = "KT_MODE")
    private String ktMode;

    @Column(name = "KT_DOCUMENT_PATH")
    private String ktDocumentPath;

    @Column(name = "STATUS")
    private String status;

    @Column(name ="IS_Deleted")
    private String isDeleted;


    @Column(name = "REMARKS")
    private String remarks;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "KT_HANDOVER_ID", insertable = false, updatable = false)
    @JsonBackReference
    private EmpExitKtHandover ktHandover;
}
