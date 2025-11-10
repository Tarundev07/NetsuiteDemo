package com.atomicnorth.hrm.tenant.domain.asset;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "category_master")
public class CategoryMaster extends AbstractAuditingEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_ID")
    private Integer categoryId;

    @Column(name = "CATEGORY_NAME")
    private String categoryName;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "REMARK")
    private String remarks;

    @OneToMany(mappedBy = "categoryMaster", fetch = FetchType.LAZY)
    private List<AssetCategory> assetCategories;
}
