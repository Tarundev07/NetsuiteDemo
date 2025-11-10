package com.atomicnorth.hrm.tenant.repository.MasterAddressRepo;

import com.atomicnorth.hrm.tenant.domain.MasterAddress.AddressMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressMasterRepository extends JpaRepository<AddressMaster, Integer>, JpaSpecificationExecutor<AddressMaster> {
}

