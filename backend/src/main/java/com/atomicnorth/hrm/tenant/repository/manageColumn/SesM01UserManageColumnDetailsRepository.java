package com.atomicnorth.hrm.tenant.repository.manageColumn;

import com.atomicnorth.hrm.tenant.domain.manageColumn.SesM01UserManageColumnDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SesM01UserManageColumnDetailsRepository extends JpaRepository<SesM01UserManageColumnDetails, Integer> {

}
