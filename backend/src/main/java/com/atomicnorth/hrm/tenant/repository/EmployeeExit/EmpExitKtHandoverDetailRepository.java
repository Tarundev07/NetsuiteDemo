package com.atomicnorth.hrm.tenant.repository.EmployeeExit;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitKtHandoverDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface EmpExitKtHandoverDetailRepository  extends JpaRepository<EmpExitKtHandoverDetail,Integer>{

    List<EmpExitKtHandoverDetail> findByKtHandoverId(Integer ktHandoverId);
    List<EmpExitKtHandoverDetail> findByKtHandoverIdAndIsDeleted(Integer ktHandoverId, String isDeleted);


}

