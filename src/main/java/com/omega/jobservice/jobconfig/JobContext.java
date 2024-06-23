package com.omega.jobservice.jobconfig;

import com.omega.jobservice.scheduledjob.ScheduleInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "ScheduledJobs")
@IdClass(JobContextPrimaryKey.class)  // composite key
public class JobContext {
    @Id
    private long jobId = -1;

    @Id
    @Column(nullable = false)
    private Long userId = -1L;

    @Id
    @Column(nullable = false)
    private String jobName;

    private Boolean isActive;

    @Column(nullable = false)
    private Integer status = JobStatus.CREATED.getIndex();

    public Integer getStatus() {
        if (status == null) 
            status = statusEnum.getIndex();
        return status;
    }

    private Long jobServerId = 0L;
    private String executorName;
    private String timezone;
    private Long createdTime = -1L;             // job added time
    private Long nextExecutionTime = -1L;       // on job creation, set expected job start time
    private Long jobStartTime = 0L;             // on job pick, execution start time
    private Long endExecutionTime = -1L;        // maximum start time that a job can be scheduled for nextExecution
    private Integer transactionTimeout = 0;     // maximum job alive time

    // Period - Custom Scheduling (mention nextExecution startTime in Seconds) eg. Jobs that run every 30 min
    private Boolean isPeriodic;
    public boolean isPeriodic() {
        return isPeriodic != null && isPeriodic;
    }
    // start job, execute, then schedule nextExecutionTime = Instant.now().getEpochSecond() + period (in seconds)
    private Integer period = -1;

    private Integer maxExecutionCount = -1;         // maximum number of times, a job can be executed
    private Integer currentExecutionCount = 0;      // current iteration count
    private Integer jobExecutionErrorCount = 0;     // error occurred count
    private ScheduleInfo scheduleInfo;
    private Integer loggerLevel = -1;

    @Transient
    private JobStatus statusEnum = JobStatus.CREATED;

    public JobStatus getStatusEnum() {
        if (statusEnum == null) {
            statusEnum = JobStatus.JOB_STATUS_MAP.get(status);
        }
        return statusEnum;
    }
    // TODO - Provide Support for adding Conf (like QueueData)

    public String getJobKey() {
        return jobId + "|" + userId + "|" + jobName;
    }

    @Override
    public String toString() {
        return jobId + "::" + userId + "::" + jobName;
    }

    @Getter
    public enum JobStatus {
        CREATED(1),
        IN_PROGRESS(2),
        COMPLETED(3),
        ERROR_OCCURRED(4);

        JobStatus(int index) {
            this.index = index;
        }

        private final int index;
        private static final Map<Integer, JobStatus> JOB_STATUS_MAP = Collections.unmodifiableMap(initTypeMap());

        private static Map<Integer, JobStatus> initTypeMap() {
            Map<Integer, JobStatus> statusMap = new HashMap<>();
            for(JobStatus type : values()) {
                statusMap.put(type.getIndex(), type);
            }
            return statusMap;
        }
    }
}
