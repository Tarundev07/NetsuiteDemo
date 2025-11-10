package com.atomicnorth.hrm.tenant.domain.asset;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "asset_category")
public class AssetCategory extends AbstractAuditingEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ASSET_CATEGORY_ID")
    private Integer assetCategoryId;

    @Column(name = "CATEGORY_ID")
    private Integer categoryId;

    @Column(name = "ASSET_ID")
    private Integer assetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSET_ID", updatable = false, insertable = false)
    private AssetMaster assetMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID", updatable = false, insertable = false)
    private CategoryMaster categoryMaster;
}
