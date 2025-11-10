package com.atomicnorth.hrm.master.repository;

import com.atomicnorth.hrm.master.domain.TenantJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantJobStatusRepository extends JpaRepository<TenantJobStatus, Integer> {


    List<TenantJobStatus> findByStatus(String status);
}
