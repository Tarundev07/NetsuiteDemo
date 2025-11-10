package com.atomicnorth.hrm.tenant.domain.location;


import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "ses_m20_city_master")
public class CityMaster extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "CITY_ID")
    private Integer cityId;

    @Column(name = "STATE_ID")
    private Integer stateId;

    @Column(name = "CITY_NAME", length = 100)
    private String cityName;

    @Column(name = "TIME_ZONE", length = 100)
    private String timeZone;

    @Column(name = "IS_ACTIVE", length = 1, nullable = false)
    private String isActive = "Y";

}