package com.atomicnorth.hrm.tenant.domain.message;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "ses_m00_messages")
public class Message extends AbstractAuditingEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_ID")
    private Long messageId;

    @Column(name = "MODULE_ID")
    private Integer moduleId;

    @Column(name = "MODULE_FUNCTION_ID")
    private Integer moduleFunctionId;

    @Column(name = "FEATURE_ID")
    private Integer featureId;

    @Column(name = "MESSAGE_CODE")
    private String messageCode;

    @Column(name = "MESSAGE_DESCRIPTION_CODE_LANG")
    private String messageDescriptionCode;

    @Column(name = "MESSAGE_TEXT_CODE_LANG")
    private String messageTextCode;

    @Column(name = "MESSAGE_TYPE")
    private String messageType;

    @Column(name = "MESSAGE_SEVERITY")
    private String messageSeverity;

    @Column(name = "STATUS")
    private String status;
}
