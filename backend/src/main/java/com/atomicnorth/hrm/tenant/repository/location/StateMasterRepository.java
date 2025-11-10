package com.atomicnorth.hrm.tenant.repository.location;

import com.atomicnorth.hrm.tenant.domain.location.StateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateMasterRepository extends JpaRepository<StateMaster,Integer> {
    List<StateMaster> findByCountryId(Integer countryId);
}
