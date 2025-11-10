package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.ApplicationSequence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationSequenceRepository extends JpaRepository<ApplicationSequence, Integer> {

    Optional<ApplicationSequence> findByTypeAndDivision(String type, Integer division);

    Optional<ApplicationSequence> findByTypeAndDivisionIsNull(String type);

    Optional<ApplicationSequence> findByType(String type);
}
