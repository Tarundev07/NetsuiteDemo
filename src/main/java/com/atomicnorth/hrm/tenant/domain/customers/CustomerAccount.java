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
import java.util.List;

@Data
@Entity
@Table(name = "ses_m22_customer_account")
public class CustomerAccount extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCOUNT_ID")
    private Integer accountId;

    @Column(name = "ACCOUNT_CODE")
    private String accountCode;

    @Column(name = "NAME")
    private String accountName;

    @Column(name = "CUSTOMER_ID")
    private Integer customerId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "PAYMENT_TYPE")
    private String paymentType;

    @Column(name = "GST_NUMBER")
    private String gstNumber;

    @Column(name = "PIN_CODE")
    private String pinCode;

    @Column(name = "PAN_NUMBER")
    private String panNumber;

    @Column(name = "COUNTRY_ID")
    private Integer countryId;

    @Column(name = "STATE_ID")
    private Integer stateId;

    @Column(name = "CITY_ID")
    private Integer cityId;

    @Column(name = "IS_TAXABLE")
    private String isTaxable;

    @Column(name = "IS_CREDIT_ENABLE")
    private String isCreditEnable;

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
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "CUSTOMER_ID", insertable = false, updatable = false)
    private Customer customer;
}
