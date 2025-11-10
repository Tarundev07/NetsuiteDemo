package com.atomicnorth.hrm.tenant.domain.MasterAddress;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Getter
@Setter
@Entity
@Table(name = "ses_m00_addresses_master")
public class AddressMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ADDRESS_ID")
    private Integer addressId;

    @Column(name = "BRANCH_ID")
    private Integer branchId;

    @Column(name = "ADDRESS_TEXT1", columnDefinition = "TEXT")
    private String addressText1;

    @Column(name = "ADDRESS_TEXT2", columnDefinition = "TEXT")
    private String addressText2;

    @Column(name = "LANDMARK")
    private String landmark;

    @Column(name = "NEAREST_LOCATION")
    private String nearestLocation;

    @Column(name = "LATITUDE", precision = 10, scale = 8)
    private Double latitude;

    @Column(name = "LONGITUDE", precision = 11, scale = 8)
    private Double longitude;

    @Column(name = "IS_PRIMARY", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "ADDRESS_CATEGORY")
    private String addressCategory;

    @Column(name = "DIRECTION_INSTRUCTIONS", columnDefinition = "TEXT")
    private String directionInstructions;

    @Column(name = "CITY", nullable = false)
    private String city;

    @Column(name = "STATE", nullable = false)
    private String state;

    @Column(name = "COUNTRY", nullable = false)
    private String country;

    @Column(name = "PINCODE", nullable = false)
    private Integer pincode;

    @Column(name = "IS_DELETED", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "CREATION_DATE", updatable = false)
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @Column(name = "LAST_UPDATED_DATE")
    private LocalDateTime lastUpdatedDate;

    @Column(name = "C_ATTRIBUTE1")
    private String cAttribute1;

    @Column(name = "C_ATTRIBUTE2")
    private String cAttribute2;

    @Column(name = "N_ATTRIBUTE16")
    private Integer nAttribute16;

    @Column(name = "D_ATTRIBUTE26")
    private LocalDateTime dAttribute26;

    @Column(name = "RECORD_INFO", columnDefinition = "TEXT")
    private String recordInfo;

    public String getFullAddress() {
        return Stream.of(addressText1, addressText2)
                .filter(Objects::nonNull)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(" "));
    }

}
