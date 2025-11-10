package com.atomicnorth.hrm.tenant.domain.employement.employeeDocument;


import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.*;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "emp_m02_employee_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDocument extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DOC_RF_NUM")
    private Long id;

    @Column(name = "DOC_NAME")
    private String docName;

    @Column(name = "SERVER_DOC_NAME")
    private String serverDocName;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "REMARK")
    private String remark;

    @Column(name = "DOC_NUMBER")
    private String docNumber;

    @Column(name = "DOC_TYPE")
    private String docType;

    @Column(name = "EMPLOYEE_ID", nullable = false)
    private Integer employeeId;

    @Lob
    @Column(name = "DOC")
    private byte[] doc;

    @Column(name = "IS_ACTIVE")
    private String isActive;

    @Column(name = "IS_DELETED")
    private String isDeleted;


}

