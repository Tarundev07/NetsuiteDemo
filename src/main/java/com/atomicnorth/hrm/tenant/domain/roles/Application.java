package com.atomicnorth.hrm.tenant.domain.roles;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "ses_m00_applications")
public class Application implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPLICATION_ID")
    private Integer applicationId;

    @Column(name = "NAME_CODE_LANG")
    private String shortCode;
    @Column(name = "DESCRIPTION_CODE_LANG")
    private String description;
    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATION_DATE")
    private Instant creationDate;
    @Column(name = "LAST_UPDATED_DATE")
    private Instant lastUpdateDate;
    @Column(name = "CREATED_BY")
    private String createdBy;
    @Column(name = "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @Transient
    @JsonProperty
    private List<ApplicationModule> child;
}
