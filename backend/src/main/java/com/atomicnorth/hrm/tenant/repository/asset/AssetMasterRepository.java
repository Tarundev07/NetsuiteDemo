package com.atomicnorth.hrm.tenant.repository.asset;

import com.atomicnorth.hrm.tenant.domain.asset.AssetMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetMasterRepository extends JpaRepository<AssetMaster, Integer> {

    List<AssetMaster> findByAssetIdInAndAssetCategories_CategoryMaster_CategoryName(List<Integer> assetId, String categoryName);
}
