package com.atomicnorth.hrm.tenant.domain.location;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ses_m00_country_master")
public class CountryMaster extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUNTRY_ID", nullable = false)
    private Integer countryId;

    @Column(name = "COUNTRY_NAME", length = 100)
    private String countryName;

    @Column(name = "PHONE_CODE", length = 100)
    private String phoneCode;

    @Column(name = "REGION", length = 200)
    private String region;

    @Column(name = "IS_ACTIVE", nullable = false, length = 1)
    private String isActive = "Y";
}
