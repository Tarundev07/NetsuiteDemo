package com.atomicnorth.hrm.tenant.domain.asset;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "emp_asset_assignment")
public class EmpAssetAssignment extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ASSIGNMENT_ID")
    private Integer id;

    @Column(name = "ASSET_ID")
    private Integer assetId;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "ASSIGNED_DATE")
    private LocalDate assignedDate;

    @Column(name = "RETURN_DATE")
    private LocalDate returnDate;

    @Column(name = "CLEARANCE_STATUS")
    private String clearanceStatus;

    @Column(name = "CLEARANCE_ATTACHMENT")
    private String clearanceAttachment;

    @Column(name = "REMARK")
    private String remark;

    @Column(name = "REASON")
    private String reason;
}
