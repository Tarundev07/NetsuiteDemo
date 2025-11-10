package com.atomicnorth.hrm.tenant.repository.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeSkillSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeSkillSetRepo extends JpaRepository<EmployeeSkillSetEntity, Integer> {

    @Query(value = "SELECT name FROM ses_m00_skills_set where SKILL_ID=:skillId", nativeQuery = true)
    Optional<String> getSkillName(@Param("skillId") Long skillId);

    List<EmployeeSkillSetEntity> findByUsername(Integer username);

}

