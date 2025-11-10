package com.atomicnorth.hrm.tenant.repository.location;

import com.atomicnorth.hrm.tenant.domain.location.CountryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryMasterRepository extends JpaRepository<CountryMaster,Integer> {
}
