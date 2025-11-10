package com.atomicnorth.hrm.tenant.service.scheduler;

import com.atomicnorth.hrm.configuration.multitenant.MultiTenantDataSourceLookup;
import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.master.domain.DataSourceConfig;
import com.atomicnorth.hrm.master.domain.TenantJobStatus;
import com.atomicnorth.hrm.master.repository.DataSourceConfigRepository;
import com.atomicnorth.hrm.master.repository.TenantJobStatusRepository;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.LeaveAllocation;
import com.atomicnorth.hrm.tenant.domain.LeaveAllocationDetails;
import com.atomicnorth.hrm.tenant.domain.branch.LeaveTypes;
import com.atomicnorth.hrm.tenant.domain.leave.LeaveLedgerEntity;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveAllocationRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveTypeRepository;
import com.atomicnorth.hrm.tenant.repository.leave.LeaveAllocationDetailRepository;
import com.atomicnorth.hrm.tenant.repository.leave.LeaveLedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveUpdateCronService {

    private static final Logger log = LoggerFactory.getLogger(LeaveUpdateCronService.class);

    private final LeaveTypeRepository leaveTypeRepository;

    private final LeaveAllocationDetailRepository leaveAllocationDetailRepository;

    private final MultiTenantDataSourceLookup multiTenantDataSourceLookup;

    private final DataSourceConfigRepository dataSourceConfigRepository;

    private final LeaveAllocationRepository leaveAllocationRepository;

    private final EmployeeRepository employeeRepository;

    private final LeaveLedgerRepository leaveLedgerRepository;

    private final TenantJobStatusRepository tenantJobStatusRepository;

    public LeaveUpdateCronService(LeaveTypeRepository leaveTypeRepository,
                                  LeaveAllocationDetailRepository leaveAllocationDetailRepository,
                                  MultiTenantDataSourceLookup multiTenantDataSourceLookup,
                                  DataSourceConfigRepository dataSourceConfigRepository,
                                  LeaveAllocationRepository leaveAllocationRepository,
                                  EmployeeRepository employeeRepository,
                                  LeaveLedgerRepository leaveLedgerRepository,
                                  TenantJobStatusRepository tenantJobStatusRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveAllocationDetailRepository = leaveAllocationDetailRepository;
        this.multiTenantDataSourceLookup = multiTenantDataSourceLookup;
        this.dataSourceConfigRepository = dataSourceConfigRepository;
        this.leaveAllocationRepository = leaveAllocationRepository;
        this.employeeRepository = employeeRepository;
        this.leaveLedgerRepository = leaveLedgerRepository;
        this.tenantJobStatusRepository = tenantJobStatusRepository;
    }

    public void runLeaveUpdateJob() {
        List<TenantJobStatus> activeJobs = tenantJobStatusRepository.findByStatus("Y");
        List<DataSourceConfig> activeDataSources = activeJobs.stream()
                .map(job -> dataSourceConfigRepository.findById(job.getConfigId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        multiTenantDataSourceLookup.addTenantDataSources(activeDataSources);
        for (DataSourceConfig tenantConfig : activeDataSources) {
            String tenantId = tenantConfig.getId();
            try {
                TenantContextHolder.setTenantId(tenantId);
                log.info("Running leave update job for tenant: {}", tenantConfig.getName());
                updateLeaveAllocations();

            } catch (Exception e) {
                log.error("Error updating tenant {}: {}", tenantConfig.getName(), e.getMessage(), e);
            } finally {
                TenantContextHolder.clear();
            }
        }
    }

    @Transactional
    public void updateLeaveAllocations() {
        System.out.println("Leave Allocation Update Cron started at: " + LocalDateTime.now());
        try {
            List<LeaveTypes> leaveTypes = leaveTypeRepository.findAll();
            List<Employee> employees = employeeRepository.findByIsActive(Constant.EMPLOYEE_STATUS_ACTIVE);

            for (LeaveTypes leaveType : leaveTypes) {
                if (!shouldUpdateThisPeriod(leaveType.getEarnedLeaveFrequency())) continue;

                for (Employee e : employees) {
                    Optional<LeaveAllocation> leaveAllocations = leaveAllocationRepository.findByEmpId(e.getEmployeeId());
                    if (leaveAllocations.isPresent()) {
                        List<LeaveAllocationDetails> leaveAllocationDetails = leaveAllocationDetailRepository.findByLeaveAllocation(leaveAllocations.get());
                        for (LeaveAllocationDetails details : leaveAllocationDetails) {
                            double currentBalance = details.getLeaveBalance() != null ? details.getLeaveBalance() : 0.0;
                            double earnedLeave = (leaveType.getEarnedLeaveAmount() != null && leaveType.getEarnedLeaveAmount() > 0) ? leaveType.getEarnedLeaveAmount() : 0.0;
                            double newBalance = currentBalance + earnedLeave;
                            details.setLastUpdatedDate(Instant.now());
                            details.setLeaveBalance(newBalance);
                            details.setCreatedBy("");
                            details.setLastUpdatedBy("");
                            leaveAllocationDetailRepository.save(details);

                            LeaveLedgerEntity ledger = new LeaveLedgerEntity();
                            ledger.setLeaveCode(details.getLeaveCode());
                            ledger.setEmpId(e.getEmployeeId());
                            ledger.setTransactionBalance(earnedLeave);
                            ledger.setTransactionType(Constant.EMPLOYEE_LEAVE_CREDIT);
                            ledger.setRemark(Constant.EMPLOYEE_LEAVE_REMARK);
                            ledger.setCreatedBy("");
                            ledger.setLastUpdatedBy("");
                            leaveLedgerRepository.save(ledger);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TenantContextHolder.clear();
        }
    }
    private boolean shouldUpdateThisPeriod(String frequency) {
        int month = LocalDateTime.now().getMonthValue();
        switch (frequency.toUpperCase()) {
            case "MONTHLY":
                return true;
            case "QUARTERLY":
                return (month == 1 || month == 4 || month == 7 || month == 10);
            case "YEARLY":
                return (month == 1);
            default:
                return false;
        }
    }
}
