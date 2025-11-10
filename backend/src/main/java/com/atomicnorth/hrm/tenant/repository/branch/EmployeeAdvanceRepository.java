package com.atomicnorth.hrm.tenant.repository.branch;

import com.atomicnorth.hrm.tenant.domain.branch.EmployeeAdvance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeAdvanceRepository extends JpaRepository<EmployeeAdvance, Integer> {
    @Query("SELECT e FROM EmployeeAdvance e LEFT JOIN Employee em ON em.employeeId = e.employeeId  WHERE " +
            "(:searchField = 'employeeId' AND LOWER(em.firstName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(em.lastName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(CONCAT(em.firstName, ' ', em.lastName)) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR" +
            "(:searchField = 'postingDate' AND CAST(e.postingDate AS string) LIKE %:searchKeyword%) OR " +
            "(:searchField = 'company' AND e.company LIKE %:searchKeyword%) OR " +
            "(:searchField = 'purpose' AND e.purpose LIKE %:searchKeyword%) OR " +
            "(:searchField = 'advanceAmount' AND CAST(e.advanceAmount AS string) LIKE %:searchKeyword%) OR " +
            "(:searchField = 'paidAmount' AND CAST(e.paidAmount AS string) LIKE %:searchKeyword%) OR " +
            "(:searchField = 'pendingAmount' AND CAST(e.pendingAmount AS string) LIKE %:searchKeyword%) OR " +
            "(:searchField = 'claimedAmount' AND CAST(e.claimedAmount AS string) LIKE %:searchKeyword%) OR " +
            "(:searchField = 'returnedAmount' AND CAST(e.returnedAmount AS string) LIKE %:searchKeyword%) OR " +
            "(:searchField = 'accounting' AND e.accounting LIKE %:searchKeyword%) OR " +
            "(:searchField = 'bankAccount' AND e.bankAccount LIKE %:searchKeyword%) OR " +
            "(:searchField = 'repayUnclaimedAmount' AND e.repayUnclaimedAmount LIKE %:searchKeyword%) OR " +
            "(:searchField = 'moreInfo' AND e.moreInfo LIKE %:searchKeyword%)")
    Page<EmployeeAdvance> searchEmployeeAdvance(
            @Param("searchKeyword") String searchKeyword,
            @Param("searchField") String searchField,
            Pageable pageable);

}
