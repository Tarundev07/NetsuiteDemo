package com.atomicnorth.hrm.tenant.service.dto.branch;

/*public class BranchWithAddressDto {
}*/


import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.service.dto.MasterAddressDTO.AddressRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchWithAddressDto extends AbstractAuditingEntity<Long> {

    // Branch details
    private Integer id;
    private String name;
    private String isActive;
    private String code;
    private Date startDate;

    // Address details
    private AddressRequestDTO address;
}

