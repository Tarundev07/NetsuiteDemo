package com.atomicnorth.hrm.tenant.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(exclude = "leaveAllocationDetails")
@Table(name = "emp_leave_allocation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LeaveAllocation extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EMP_ID")
    private Integer empId;

    @Column(name = "TOTAL_LEAVE")
    private Double totalLeave;

    @Column(name = "IS_ACTIVE", length = 10, nullable = false)
    private String isActive = "A";
    @OneToMany(mappedBy = "leaveAllocation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LeaveAllocationDetails> leaveAllocationDetails;
}



