package com.atomicnorth.hrm.tenant.service.dto.branch;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.domain.branch.Branch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchDto extends AbstractAuditingEntity<Long> {


    private Integer id;
    private String name;
    private String isActive;
    private String code;
    private Date startDate;
    private Integer addressId;
    private String address;


    public BranchDto(Branch branch) {
        this.id = branch.getId();
        this.name = branch.getName();
        this.isActive = branch.getIsActive();
        this.code = branch.getCode();
        this.startDate = branch.getStartDate();
        this.addressId = branch.getAddressId();
    }
}
