package com.atomicnorth.hrm.tenant.domain.employement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.util.Enum.Active;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ses_m25_employee_group")
public class EmployeeGroup extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Column(name = "GROUP_ID")
    private Long groupId;

    @Column(name = "EMP_ID")
    private String empId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ISACTIVE", length = 1)
    private Active isActive;
}
