package com.atomicnorth.hrm.tenant.repository.holiday;

import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface HolidaysCalendarRepository extends JpaRepository<HolidaysCalendar, Integer> {

    Optional<HolidaysCalendar> findByName(String name);

    Optional<HolidaysCalendar> findByHolidayCalendarId(Integer id);

    //  ADD THIS FOR PROJECT:
    List<HolidaysCalendar> findByNameContainingIgnoreCase(String name);



    @Query(value = "SELECT HOLIDAY_CALENDAR_ID, NAME " +
            "FROM ses_m06_hoilday_calendar",
            nativeQuery = true)
    List<Object[]> findHolidayIdAndName();
}