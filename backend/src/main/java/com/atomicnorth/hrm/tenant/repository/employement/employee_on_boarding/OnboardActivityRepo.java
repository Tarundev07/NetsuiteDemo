package com.atomicnorth.hrm.tenant.repository.employement.employee_on_boarding;

import com.atomicnorth.hrm.tenant.domain.employement.employee_on_boarding.OnboardActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OnboardActivityRepo extends JpaRepository<OnboardActivity, Integer> {

    Optional<OnboardActivity> findById(Integer activityId);

    @Query("SELECT oa FROM OnboardActivity oa WHERE oa.onboardingId = :id")
    List<OnboardActivity> findByOnboardingId(@Param("id") Integer id);
}