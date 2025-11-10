package com.atomicnorth.hrm.tenant.domain.attendance;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "ses_m06_shift_detail")
public class SupraShiftDetailEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHIFT_DETAIL_ID")
    private Integer shiftDetailId;

    @Column(name = "IS_ACTIVE")
    private String isActive;

    @Column(name = "START_DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column(name = "DATE_CHANGE_FLAG")
    private String dateChangeFlag;

    @Column(name = "SHIFT_START_TIME")
    private String shiftStartTime;

    @Column(name = "SHIFT_END_TIME")
    private String shiftEndTime;

    @Column(name = "MIN_START_HOUR")
    private Integer minStartHour;

    @Column(name = "MAX_END_HOUR")
    private Integer maxEndHour;

    @Column(name = "WEEK_DAY")
    private String weekDay;

    @Column(name = "WEEKLY_OFF")
    private String weeklyOff;


    @ManyToOne
    @JoinColumn(name = "SHIFT_ID", nullable = false)
    @JsonBackReference
    private SupraShiftEntity supraShift;
}
