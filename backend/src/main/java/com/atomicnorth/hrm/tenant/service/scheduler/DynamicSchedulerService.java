package com.atomicnorth.hrm.tenant.service.scheduler;

import com.atomicnorth.hrm.master.domain.TenantJobStatus;
import com.atomicnorth.hrm.master.repository.TenantJobStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class DynamicSchedulerService {

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final TenantJobStatusRepository jobStatusRepo;

    private final LeaveUpdateCronService leaveUpdateCronService;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    @Autowired
    public DynamicSchedulerService(TenantJobStatusRepository jobStatusRepo, LeaveUpdateCronService leaveUpdateCronService) {
        this.jobStatusRepo = jobStatusRepo;
        this.leaveUpdateCronService = leaveUpdateCronService;
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("TenantJob-");
        scheduler.initialize();
    }
    @PostConstruct
    public void init() {
        List<TenantJobStatus> activeJobs = jobStatusRepo.findByStatus("Y");


        for (TenantJobStatus job : activeJobs) {
            job.setLastJobRun(LocalDateTime.now());
            jobStatusRepo.save(job);
            scheduleJob(job);
        }
    }
    public void scheduleJob(TenantJobStatus job) {
        String key = job.getConfigId() + "_" + job.getJobCode();
        if (scheduledTasks.containsKey(key)) {
            System.out.println("Job already scheduled: " + key);
            return;
        }

        Runnable task = () -> executeJob(job);

        ScheduledFuture<?> future = scheduler.schedule(task, new CronTrigger(job.getCronExpression()));
        scheduledTasks.put(key, future);

        System.out.println("Scheduled job: " + job.getJobCode() +
                " for Tenant: " + job.getConfigId() +
                " using cron: " + job.getCronExpression());
    }
    public void stopJob(TenantJobStatus job) {
        String key = job.getConfigId() + "_" + job.getJobCode();
        ScheduledFuture<?> future = scheduledTasks.remove(key);

        if (future != null) {
            future.cancel(false);
            System.out.println("Stopped job: " + key);
        } else {
            System.out.println("No running job found for: " + key);
        }
    }
    private void executeJob(TenantJobStatus job) {
        System.out.println("▶ Running job: " + job.getJobCode() +
                " for Tenant: " + job.getConfigId() +
                " at " + LocalDateTime.now());
        switch (job.getJobCode()) {
            case "LEAVE_UPDATE":
                leaveUpdateCronService.runLeaveUpdateJob();
                break;
            case "TIMESHEET_CHECK":
                // timesheet check logic
                break;
            case "ATTENDANCE_CHECK":
                // attendance check logic
                break;
            case "BIRTHDAY_ALERT":
                // birthday alert logic
                break;
            default:
                System.out.println("Unknown job: " + job.getJobCode());
        }
    }
}
