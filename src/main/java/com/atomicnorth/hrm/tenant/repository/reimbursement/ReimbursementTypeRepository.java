package com.atomicnorth.hrm.tenant.repository.reimbursement;

import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimbursementTypeRepository extends JpaRepository<ReimbursementType, String> {
    @Query(value = "SELECT EXPENSE_CODE, NAME, DESCRIPTION, ENABLE_FLAG FROM ses_m00_rms_type", nativeQuery = true)
    List<Object[]> getExpenseTypes();
}
