package com.atomicnorth.hrm.tenant.repository.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.StaffPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffPlanRepository extends JpaRepository<StaffPlan, Integer> {

    @EntityGraph(attributePaths = "staffPlanDetails")
    Optional<StaffPlan> findById(Long id);
}
