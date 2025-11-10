package com.atomicnorth.hrm.tenant.repository.designation;

import com.atomicnorth.hrm.tenant.domain.designation.SkillSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillSetRepository extends JpaRepository<SkillSet, Integer> {
}
