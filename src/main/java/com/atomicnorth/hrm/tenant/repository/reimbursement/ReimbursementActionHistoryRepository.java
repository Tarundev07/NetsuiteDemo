package com.atomicnorth.hrm.tenant.repository.reimbursement;

import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementActionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReimbursementActionHistoryRepository extends JpaRepository<ReimbursementActionHistory, Integer> {
}
