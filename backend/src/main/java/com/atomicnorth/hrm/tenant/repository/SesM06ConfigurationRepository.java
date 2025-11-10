package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.lookup.LookupType;
import com.atomicnorth.hrm.tenant.repository.translation.LookupTypeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SesM06ConfigurationRepository extends JpaRepository<LookupType, Integer> {

    Page<LookupType> findByLookupIdOrAppModuleOrModuleFunction(Integer id, Long module, Long function, Pageable pageable);

    List<LookupTypeProjection> findAllBy();

}
