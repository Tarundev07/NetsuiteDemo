package com.atomicnorth.hrm.tenant.domain.jobOpening;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.util.Enum.Active;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "ses_m04_staff_plan_details")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StaffPlanDetails extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DESIGNATION_ID", nullable = false)
    private Integer designationId;

    @Column(name = "DEPARTMENT_ID", nullable = false)
    private Integer departmentId;

    @Column(name = "JOB_REQUISITION_ID")
    private Integer jobRequisitionId;

    @Column(name = "REQUIRED_VACANCY", nullable = false)
    private Integer requiredVacancies;

    @Column(name = "ESTIMATED_COST", nullable = false)
    private Integer estimateCost;

    @Column(name = "IS_ACTIVE")
    private Active isActive;

    @Transient
    private String designationName;

    @Transient
    private String jobRequisitionName;

    @ManyToOne
    @JoinColumn(name = "STAFF_PLAN_ID", nullable = false)
    private StaffPlan staffPlanId;

    @ManyToOne
    @JoinColumn(name = "DESIGNATION_ID", referencedColumnName = "DESIGNATION_ID", insertable = false, updatable = false)
    private Designation designation;

    @ManyToOne
    @JoinColumn(name = "JOB_REQUISITION_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private JobRequisition jobRequisition;
}
