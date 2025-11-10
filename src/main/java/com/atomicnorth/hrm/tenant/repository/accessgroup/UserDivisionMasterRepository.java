package com.atomicnorth.hrm.tenant.repository.accessgroup;

import com.atomicnorth.hrm.tenant.domain.accessgroup.SesM00UserDivisionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDivisionMasterRepository extends JpaRepository<SesM00UserDivisionMaster, Long>, JpaSpecificationExecutor<SesM00UserDivisionMaster> {

    Optional<SesM00UserDivisionMaster> findById(Long id);

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndDivisionIdNot(String name, Long id);
    Optional<SesM00UserDivisionMaster> findByDivisionId(Long requestId);

    List<SesM00UserDivisionMaster> findByActiveFlag(String activeFlag);

}