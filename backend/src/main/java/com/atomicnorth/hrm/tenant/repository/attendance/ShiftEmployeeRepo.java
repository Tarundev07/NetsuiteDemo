package com.atomicnorth.hrm.tenant.repository.attendance;

import com.atomicnorth.hrm.tenant.domain.attendance.ShiftEmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShiftEmployeeRepo extends JpaRepository<ShiftEmployeeEntity, Integer> {

    @Transactional
    @Modifying
    @Query(value = "delete from ses_m07_shift_emp where SHIFT_EMP_ID in (:paramMap)", nativeQuery = true)
    void deleteByShiftEmpId(@Param("paramMap") Set<String> shiftToBeUpdated);

    List<ShiftEmployeeEntity> findByEmployeeId(Integer employeeId);

    List<ShiftEmployeeEntity> findByEmployeeIdAndIsActive(Integer employeeId, String isActive);

    Optional<ShiftEmployeeEntity> findByShiftId(Integer shiftId);

    List<ShiftEmployeeEntity> findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsActive(
            Integer employeeId, Date endDate, Date startDate, String isActive);

    List<ShiftEmployeeEntity> findByShiftIdAndIsActive(Integer shiftId, String isActive);

    List<ShiftEmployeeEntity> findByEmployeeIdInAndIsActive(Set<Integer> employeeIds, String isActive);
}
