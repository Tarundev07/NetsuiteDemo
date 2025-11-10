package com.atomicnorth.hrm.tenant.repository.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.StaffPlanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffPlanDetailsRepository extends JpaRepository<StaffPlanDetails, Integer> {

    Optional<StaffPlanDetails> findById(Long id);

    @Query(value = "SELECT spd.ID, spd.DESIGNATION_ID, spd.REQUIRED_VACANCY, spd.ESTIMATED_COST, spd.STAFF_PLAN_ID, spd.IS_ACTIVE, sp.STAFF_NAME, spd.DEPARTMENT_ID\n" +
            "FROM ses_m04_staff_plan_details spd \n" +
            "LEFT JOIN ses_m04_staff_plan sp ON spd.STAFF_PLAN_ID = sp.ID\n" +
            "WHERE spd.DESIGNATION_ID = :id AND spd.IS_ACTIVE = 0;", nativeQuery = true)
    List<Object[]> getPlanByDesignation(@Param("id") Integer id);

}
