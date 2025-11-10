package com.atomicnorth.hrm.tenant.repository.branch;

import com.atomicnorth.hrm.tenant.domain.branch.HRSettings;
import com.atomicnorth.hrm.tenant.service.dto.branch.HRSettingDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface HRSettingsRepository extends JpaRepository<HRSettings, Integer>, JpaSpecificationExecutor<HRSettings> {
    Optional<HRSettings> findByEmployeeId(Integer employeeId);

    boolean existsByEmployeeIdAndIdNot(Integer employeeId, Integer id);

    HRSettingDto findById(Long id);

}
