package com.atomicnorth.hrm.tenant.service.dto.reimbursement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Data
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ExpenseDTO extends AbstractAuditingEntity<Long> {

    List<ExpenseDocumentDTO> expenseDocumentList;
    private Integer requestId;
    private String expenseCode;
    private String expenseName;
    private String billNumber;
    @DateTimeFormat
    private String billDate;
    private double billAmount;
    private double requestAmount;
    private double approvedAmount;
    private String applicantRemark;
    private String reviewerRemark;
    private String approverRemark;
    private String expDocName;
    private String codeName;
    private Integer projectId;
    private String taskId;
    private String mappingFlag;
}
