package com.atomicnorth.hrm.tenant.repository.asset;

import com.atomicnorth.hrm.tenant.domain.asset.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Integer> {
}
