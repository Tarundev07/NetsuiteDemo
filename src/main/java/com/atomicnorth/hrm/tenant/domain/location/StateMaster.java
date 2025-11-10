package com.atomicnorth.hrm.tenant.domain.location;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ses_m20_state_master")
@Data
public class StateMaster extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STATE_ID")
    private Integer stateId;

    @Column(name = "COUNTRY_ID")
    private Integer countryId;

    @Column(name = "STATE_NAME")
    private String stateName;

    @Column(name = "TIME_ZONE")
    private String timeZone;

    @Column(name = "IS_ACTIVE", nullable = false)
    private String isActive;

}

