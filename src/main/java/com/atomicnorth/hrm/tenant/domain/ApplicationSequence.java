package com.atomicnorth.hrm.tenant.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "application_sequence")
public class ApplicationSequence extends AbstractAuditingEntity<Integer> implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "TYPE", nullable = false)
    private String type;

    @Column(name = "PREFIX", nullable = false)
    private String prefix;

    @Column(name = "START_NUMBER", nullable = false)
    private Integer startNumber;

    @Column(name = "CURRENT_NUMBER", nullable = false)
    private Integer currentNumber;

    @Column(name = "INCREMENT")
    private Integer increment;

    @Column(name = "DIVISION")
    private Integer division;

    @Version
    @Column(name = "VERSION")
    private Integer version;

}