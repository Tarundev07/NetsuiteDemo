package com.atomicnorth.hrm.tenant.domain.employement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ses_m00_addresses")
public class EmployeeAddress extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ADDRESS_ID")
    private Integer addressId; // Id or Primary Key

    @Column(name = "USER_NAME")
    private Integer username;

    @Column(name = "ADDRESS_TYPE_CODE", length = 50)
    private String addressTypeCode;

    @Column(name = "CITY_ID")
    private Integer cityId;

    @Column(name = "COUNTRY_ID")
    private Integer countryId;

    @Column(name = "PINCODE")
    private Integer pincode;

    @Column(name = "STATE_ID")
    private Integer stateId;

    @Column(name = "ADDRESS_TEXT", length = 5000)
    private String addressText;

    @Column(name = "IS_DELETED", length = 1)
    private String isDeleted = "N";

    @Column(name = "ENTITY_ID")
    private Integer entityId;

    @Column(name = "CLIENT_ID")
    private Integer clientId;

    @Column(name = "LAST_UPDATE_SESSION_ID", length = 500)
    private String lastUpdateSessionId;

}
