package com.atomicnorth.hrm.tenant.domain.customers;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.location.CityMaster;
import com.atomicnorth.hrm.tenant.domain.location.CountryMaster;
import com.atomicnorth.hrm.tenant.domain.location.StateMaster;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "ses_m22_customer")
@Data
public class Customer extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUSTOMER_ID")
    private Integer customerId;

    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @Column(name = "SALES_REPRESENTATIVE_NUMBER")
    private Integer salesRepresentativeNumber;

    @Column(name = "CUSTOMER_DESCRIPTION")
    private String customerDescription;

    @Column(name = "CUSTOMER_TYPE")
    private String customerType;

    @Column(name = "EFFECTIVE_START_DATE")
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;

    @Column(name = "COUNTRY_ID")
    private Integer countryId;

    @Column(name = "CITY_ID")
    private Integer cityId;
    @Column(name = "STATE_ID")
    private Integer stateId;

    @Column(name = "PRIMARY_CURRENCY")
    private String primaryCurrency;

    @Column(name = "CUSTOMER_WEBSITE_URL")
    private String customerWebsiteUrl;

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
    @JoinColumn(name = "SALES_REPRESENTATIVE_NUMBER", referencedColumnName = "EMPLOYEE_ID", insertable = false, updatable = false)
    private Employee salesRepresentative;

}
