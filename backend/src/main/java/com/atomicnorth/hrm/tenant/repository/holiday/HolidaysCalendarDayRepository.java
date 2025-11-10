package com.atomicnorth.hrm.tenant.repository.holiday;

import com.atomicnorth.hrm.tenant.domain.employement.employee_on_boarding.OnboardApplicant;
import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendarDay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface HolidaysCalendarDayRepository extends JpaRepository<HolidaysCalendarDay, Integer> {

    List<HolidaysCalendarDay> findByHolidayCalendarId(Integer holidayCalendarId);

    Optional<HolidaysCalendarDay> findByHolidayCalendarIdAndHolidayDate(Integer holidayCalendarId, LocalDate holidayDate);

    long countByHolidayCalendarId(int holidayCalendarId);

    Page<HolidaysCalendarDay> findAll(Specification<HolidaysCalendarDay> spec, Pageable pageable);

    List<HolidaysCalendarDay> findByHolidayCalendarIdIn(Set<Integer> holidayIds);

}

