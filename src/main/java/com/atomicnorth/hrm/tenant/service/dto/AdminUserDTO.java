package com.atomicnorth.hrm.tenant.service.dto;

import com.atomicnorth.hrm.tenant.domain.User;
import com.atomicnorth.hrm.tenant.service.dto.accessgroup.UserAssociationDataDTO;
import com.atomicnorth.hrm.tenant.service.dto.roles.RoleDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.PastOrPresent;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AdminUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String email;
    private String password;
    private Boolean activated;
    private Date resetDate;
    private String isActive;
    private Date endDate;
    private Date startDate;
    private String displayName;
    private String mobileNo;
    private String isLocked;
    private String authType;
    private String mobileCountryCode;
    private String isEmailVerified;
    private Date emailTokenGeneratedDate;
    private Integer consecutiveInvalidAttempt;
    private Date lockedDate;
    private Integer passwordStrength;
    private String resetToken;
    private Date resetTokenExpiresDate;
    private Date passwordResetDate;
    private String recordInfo;
    private String reportingmanager;
    private Integer hrManager;
    private String userCode;
    // private Set<Integer> authorities;
    private Set<Integer> roles;
    private List<RoleDTO> roleList;
    // Associations
    private List<UserAssociationDataDTO> associationData;
    private String ssoUserId;


    // No need for @OneToMany or EAGER fetch here, it's a DTO, not an entity
    // private List<UserAssociationDataDTO> associationData;

    public AdminUserDTO() {
        // Empty constructor needed for Jackson.
    }

    public AdminUserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.activated = user.getActivated();
        // this.resetDate = Date.from(user.getResetDate());
        this.isActive = user.getIsActive();
        this.startDate = user.getStartDate();
        this.endDate = user.getEndDate();
        this.displayName = user.getDisplayName();
        this.mobileNo = user.getMobileNo();
        this.isLocked = user.getIsLocked();
        this.authType = user.getAuthType();
        this.mobileCountryCode = user.getMobileCountryCode();
        this.isEmailVerified = user.getIsEmailVerified();
        this.emailTokenGeneratedDate = user.getEmailTokenGeneratedDate();
        this.lockedDate = user.getLockedDate();
        this.passwordStrength = user.getPasswordStrength();
        this.resetToken = user.getResetToken();
        this.resetTokenExpiresDate = user.getResetTokenExpiresDate();
        this.consecutiveInvalidAttempt = user.getConsecutiveInvalidAttempt();
        this.passwordResetDate = user.getPasswordResetDate();
        this.recordInfo = user.getRecordInfo();
        this.ssoUserId = user.getKeycloakId();
        this.associationData = user.getAssociations().stream().collect(Collectors.toList());
        this.roles = user.getAuthorities().stream().map(auth -> auth.getRoleId()).collect(Collectors.toSet());
        this.roleList = user.getAuthorities().stream().map(RoleDTO::new).collect(Collectors.toList());


    }

}




