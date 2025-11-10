package com.atomicnorth.hrm.tenant.domain.asset;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "asset_master")
public class AssetMaster extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ASSET_ID")
    private Integer assetId;

    @Column(name = "ASSET_CODE", nullable = false, unique = true)
    private String assetCode;

    @Column(name = "ASSET_NAME", nullable = false)
    private String assetName;

    @Column(name = "ASSET_TYPE")
    private String assetType;

    @Column(name = "ASSET_DESCRIPTION")
    private String assetDescription;

    @Column(name = "SERIAL_NUMBER")
    private String serialNumber;

    @Column(name = "PURCHASE_DATE")
    private LocalDate purchaseDate;

    @Column(name = "STATUS")
    private String status;

    @OneToMany(mappedBy = "assetMaster",fetch = FetchType.LAZY)
    private List<AssetCategory> assetCategories;
}
