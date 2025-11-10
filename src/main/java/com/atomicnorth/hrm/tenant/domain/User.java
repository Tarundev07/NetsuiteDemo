package com.atomicnorth.hrm.tenant.domain;

import com.atomicnorth.hrm.tenant.domain.accessgroup.UserAssociation;
import com.atomicnorth.hrm.tenant.domain.roles.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@Data
@NamedEntityGraph(name = "userAssociate", attributeNodes = {@NamedAttributeNode("associations"), @NamedAttributeNode("authorities")})
@Table(name = "ses_m04_user")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User extends AbstractAuditingEntity<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USER_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "ACTIVATED")
    private Boolean activated;

    @Column(name = "RESET_KEY", length = 20)
    private String resetKey;

    @Column(name = "RESET_DATE")
    private Instant resetDate;

    @Column(name = "IS_ACTIVE", length = 1)
    private String isActive;

    @Column(name = "END_DATE")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "START_DATE")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "DISPLAY_NAME", length = 50)
    private String displayName;

    @Column(name = "MOBILE_NO", length = 50)
    private String mobileNo;

    @Column(name = "IS_LOCKED", length = 50)
    private String isLocked;

    @Column(name = "AUTH_TYPE", length = 50)
    private String authType;

    @Column(name = "MOBILE_COUNTRY_CODE", length = 10)
    private String mobileCountryCode;

    @Column(name = "IS_EMAIL_VERIFIED", length = 10)
    private String isEmailVerified;

    @Column(name = "EMAIL_TOKEN", length = 50)
    private String emailToken;

    @Column(name = "EMAIL_TOKEN_GENERATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date emailTokenGeneratedDate;

    @Column(name = "CONSECUTIVE_INVALID_ATTEMPT")
    private Integer consecutiveInvalidAttempt;

    @Column(name = "LOCKED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockedDate;

    @Column(name = "PASSWORD_GENERATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordGeneratedDate;

    @Column(name = "PASSWORD_STRENGTH")
    private Integer passwordStrength;

    @Column(name = "RESET_TOKEN", length = 250)
    private String resetToken;

    @Column(name = "RESET_TOKEN_EXPIRES_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date resetTokenExpiresDate;

    @Column(name = "PASSWORD_RESET_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordResetDate;

    @Column(name = "SSO_USER_UNIQUE_KEY", length = 50, unique = true)
    private String keycloakId;

    @Column(name = "SSO_USER_ACTIVATION", length = 50, unique = true)
    private String keycloakUserActivation;

    @Column(name="SECRET_KEY")
    private String secretKey;
    @Column(name="TWO_FA_VERIFIED")
    private boolean isTwoFaVerified;



    @JsonManagedReference
    @OneToMany(targetEntity = UserAssociation.class, mappedBy = "userMasterTest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAssociation> associations;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ses_m00_user_roles",
            joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")},
            inverseJoinColumns = {@JoinColumn(name = "ROLE_ID", referencedColumnName = "ROLE_ID")}
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @BatchSize(size = 20)
    private Set<Role> authorities = new HashSet<>();

    @Transient
    @JsonProperty("roles")
    private Set<Integer> roles;

    public User() {
    }

}
