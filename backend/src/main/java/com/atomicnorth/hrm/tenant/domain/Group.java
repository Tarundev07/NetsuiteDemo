package com.atomicnorth.hrm.tenant.domain;

import com.atomicnorth.hrm.util.Enum.Active;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ses_m00_group")
public class Group extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_ID", nullable = false)
    private Long id;

    @Column(name = "GROUP_CODE", nullable = false, unique = true, length = 50)
    private String groupCode;

    @Column(name = "GROUP_NAME", nullable = false, length = 50)
    private String groupName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ISACTIVE", length = 1)
    private Active isActive;

    public Long getId() {
        return id;
    }
}
