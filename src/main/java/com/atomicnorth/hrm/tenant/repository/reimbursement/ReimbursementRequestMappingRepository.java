package com.atomicnorth.hrm.tenant.repository.reimbursement;

import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementRequestMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimbursementRequestMappingRepository extends JpaRepository<ReimbursementRequestMapping, Integer> {

    @Query(value = "SELECT m.expense_Code, t.Name,m.bill_date, m.request_Amount, m.approved_Amount, m.applicant_Remark,m.bill_number,m.bill_amount,m.attachment,m.project_id,m.task_id FROM ses_m00_rms_request_mapping m left join ses_m00_rms_type t on m.expense_Code=t.expense_Code where m.request_number=:reqNumber", nativeQuery = true)
    List<Object[]> findReimbursementRequestView(@Param("reqNumber") String reqNumber);

    List<ReimbursementRequestMapping> findByRequstnumber(String requestNumber);

    ReimbursementRequestMapping findByRequestId(Integer requestId);

    @Query(value = "SELECT \n" +
            "    r.EMPLOYEE_ID AS employeeId,\n" +
            "    (SELECT DISPLAY_NAME FROM emp_employee_master WHERE employee_id = r.EMPLOYEE_ID) AS raisedBy,\n" +
            "    r.REQUESTED_AMOUNT AS claimed,\n" +
            "    r.APPROVED_AMOUNT AS approved,\n" +
            "    r.REVIEWER_REMARK AS reviewer,\n" +
            "    r.request_number AS reqNo,\n" +
            "    MAX(rrm.REQUEST_ID) AS REQUEST_ID,\n" +
            "    MAX(rrm.CREATION_DATE) AS Created_On, \n" +
            "    MAX(rrm.LAST_UPDATED_DATE) AS Updated_On,\n" +
            "    MAX(rrm.APPLICANT_REMARK) AS Remark,\n" +
            "    MAX(r.STATUS) AS requestStatus,\n" +
            "    r.sr_no AS srNo\n" +
            "FROM ses_m00_rms_request r\n" +
            "LEFT JOIN ses_m00_rms_request_mapping rrm \n" +
            "    ON r.REQUEST_NUMBER = rrm.REQUEST_NUMBER\n" +
            "WHERE rrm.PROJECT_ID =:projectId AND rrm.MAPPING_FLAG = 'Y'\n" +
            "GROUP BY r.REQUEST_NUMBER;\n", nativeQuery = true)
    List<Object[]> fetchReimbursementRequests(@Param("projectId") Integer projectId);
}
