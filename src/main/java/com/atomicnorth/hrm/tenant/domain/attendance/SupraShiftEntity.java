package com.atomicnorth.hrm.tenant.domain.attendance;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ses_m06_shift")
@Data
public class SupraShiftEntity extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHIFT_ID")
    private Integer shiftId;

    @Column(name = "CALENDAR_ID")
    private Integer calendarId;

    @Column(name = "SHIFT_CODE")
    private String shiftCode;

    @Column(name = "COLOR_CODE")
    private String colorCode;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "GENERAL_START_TIME")
    private String generalStartTime;

    @Column(name = "GENERAL_END_TIME")
    private String generalEndTime;

    @Column(name = "DATE_CHANGE_FLAG")
    private String dateChangeFlag;

    @Column(name = "IS_ACTIVE")
    private String isActive;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "IS_DEFAULT")
    private String isDefault;


    @EqualsAndHashCode.Exclude
    @JsonManagedReference
    @OneToMany(mappedBy = "supraShift", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupraShiftDetailEntity> shiftDetails;

}
