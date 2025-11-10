package com.atomicnorth.hrm.tenant.domain.jobOpening;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.util.Enum.Active;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "ses_m04_staff_plan")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StaffPlan extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "STAFF_NAME", nullable = false, length = 100)
    private String staffName;

    @Enumerated(EnumType.STRING)
    @JsonEnumDefaultValue
    @Column(name = "ISACTIVE", length = 1)
    private Active isActive;

    @Column(name = "EFFECTIVE_START_DATE", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date effectiveEndDate;

    @OneToMany(mappedBy = "staffPlanId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<StaffPlanDetails> staffPlanDetails = new ArrayList<>();
}
