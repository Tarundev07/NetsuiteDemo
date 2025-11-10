package com.atomicnorth.hrm.tenant.repository.leave;

import com.atomicnorth.hrm.tenant.domain.leave.LeaveLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LeaveLedgerRepository extends JpaRepository<LeaveLedgerEntity, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ses_m00_user_activity_log (ACTIVITY_CATEGORY_ID, USER_NAME, OBJECT_ID, MODULE_ID, ACTIVITY_MESSAGE, CREATED_BY, CREATION_DATE) VALUES (?1, ?2, ?3, ?4, ?5, ?6, NOW())", nativeQuery = true)
    int insertUserActivityLog(String activityCategoryId,
                              String username,
                              String objectId,
                              String moduleId,
                              String activityMessage,
                              String createdBy);

    List<LeaveLedgerEntity> findByEmpId(Integer empId);

    List<LeaveLedgerEntity> findByEmpIdAndLeaveCode(Integer empId, String leaveCode);
}