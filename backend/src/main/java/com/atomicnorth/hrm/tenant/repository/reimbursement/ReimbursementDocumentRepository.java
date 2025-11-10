package com.atomicnorth.hrm.tenant.repository.reimbursement;

import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ReimbursementDocumentRepository extends JpaRepository<ReimbursementDocument, Integer> {
    @Query(value = "select count(1) from ses_m00_rms_document where request_number=:requestNumber and expense_code=:expenseCode and doc_Flag=:docFlag", nativeQuery = true)
    int findReimbursementDocNumber(@Param("requestNumber") String requestNumber, @Param("expenseCode") String expenseCode, @Param("docFlag") String docFlag);

    @Modifying
    @Transactional
    @Query(value = "UPDATE ses_m00_rms_document rd SET rd.doc_flag = :newFlag WHERE rd.request_Number = :requestNumber " +
            "AND rd.expense_code = :expenseCode", nativeQuery = true)
    void updateDocFlag(@Param("requestNumber") String requestNumber,
                       @Param("expenseCode") String expenseCode,
                       @Param("newFlag") String newFlag);

    @Query(value = "SELECT DOCUMENT FROM ses_m00_rms_document WHERE REQUEST_ID = :requestid", nativeQuery = true)
    byte[] findExpenseByRqNo(@Param("requestid") Integer requestid);

    List<ReimbursementDocument> findByRequestId(Integer requestId);
    List<ReimbursementDocument> findByRequestNumber(String requestNumber);
}
