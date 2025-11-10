package com.atomicnorth.hrm.tenant.repository.asset;

import com.atomicnorth.hrm.tenant.domain.asset.CategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryMasterRepository extends JpaRepository<CategoryMaster, Integer> {
}
