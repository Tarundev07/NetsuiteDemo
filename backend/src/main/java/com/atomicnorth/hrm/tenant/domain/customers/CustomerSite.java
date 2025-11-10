package com.atomicnorth.hrm.tenant.domain.customers;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.domain.location.CityMaster;
import com.atomicnorth.hrm.tenant.domain.location.CountryMaster;
import com.atomicnorth.hrm.tenant.domain.location.StateMaster;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "ses_m22_customer_site")
public class CustomerSite extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SITE_ID")
    private Integer siteId;

    @Column(name = "ACCOUNT_ID")
    private Integer accountId;

    @Column(name = "SITE_CODE")
    private String siteCode;

    @Column(name = "NAME")
    private String siteName;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "COUNTRY_ID")
    private Integer countryId;

    @Column(name = "STATE_ID")
    private Integer stateId;

    @Column(name = "CITY_ID")
    private Integer cityId;

    @Column(name = "ADDRESS_1")
    private String address1;

    @Column(name = "ADDRESS_2")
    private String address2;

    @Column(name = "ADDRESS_3")
    private String address3;

    @Column(name = "PIN_CODE")
    private Integer pinCode;

    @Column(name = "PAN_NUMBER")
    private String panNumber;

    @Column(name = "LATITUDE", precision = 20, scale = 6)
    private BigDecimal latitude;

    @Column(name = "LONGITUDE", precision = 20, scale = 6)
    private BigDecimal longitude;

    @Column(name = "BILL_TO")
    private String billTo;

    @Column(name = "SHIP_TO")
    private String shipTo;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "GST_NUMBER")
    private String gstNumber;

    @Column(name = "IS_PRIMARY")
    private String isPrimary;

    @Column(name = "IS_ACTIVE")
    private String isActive;
    @ManyToOne
    @JoinColumn(name = "COUNTRY_ID", insertable = false, updatable = false)
    private CountryMaster country;
    @ManyToOne
    @JoinColumn(name = "CITY_ID", insertable = false, updatable = false)
    private CityMaster city;
    @ManyToOne
    @JoinColumn(name = "STATE_ID", insertable = false, updatable = false)
    private StateMaster state;
    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID", referencedColumnName = "ACCOUNT_ID", insertable = false, updatable = false)
    private CustomerAccount customerAccount;

}

