package com.atomicnorth.hrm.tenant.repository.location;

import com.atomicnorth.hrm.tenant.domain.location.CityMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityMasterRepository extends JpaRepository<CityMaster,Integer> {
    List<CityMaster> findByStateId(Integer stateId);

    List<CityMaster> findByStateIdIn(List<Integer> stateIds);
}
