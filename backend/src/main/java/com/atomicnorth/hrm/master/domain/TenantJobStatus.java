package com.atomicnorth.hrm.master.domain;;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "tenant_job_status")
public class TenantJobStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "CONFIG_ID", nullable = false)
    private String configId; // Tenant ID, maps to data_source_config.id

    @Column(name = "JOB_CODE", nullable = false)
    private String jobCode; // Job identifier like LEAVE_UPDATE

    @Column(name = "CRON_EXPRESSION", nullable = false)
    private String cronExpression; // Cron schedule

    @Column(name = "IS_STATUS", length = 1)
    private String status = "Y"; // 'Y' = active, 'N' = stopped

    @Column(name = "LAST_JOB_RUN")
    private LocalDateTime lastJobRun; // Last execution timestamp

    // ================= Constructors =================

    public TenantJobStatus() {
    }

    public TenantJobStatus(String configId, String jobCode, String cronExpression, String status) {
        this.configId = configId;
        this.jobCode = jobCode;
        this.cronExpression = cronExpression;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastJobRun() {
        return lastJobRun;
    }

    public void setLastJobRun(LocalDateTime lastJobRun) {
        this.lastJobRun = lastJobRun;
    }
}

