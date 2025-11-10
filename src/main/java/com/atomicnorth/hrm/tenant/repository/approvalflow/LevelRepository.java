package com.atomicnorth.hrm.tenant.repository.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LevelRepository extends JpaRepository<Level,Integer>, JpaSpecificationExecutor<Level> {

    List<Level> findByIsActive(String status);

    List<Level> findByIsActiveAndIsHr(String status, String isHr);

    List<Level> findByIsActiveAndIsManager(String status, String isManager);

    List<Level> findByIsActiveAndIsHrAndIsManager(String status, String isHr, String isManager);

}