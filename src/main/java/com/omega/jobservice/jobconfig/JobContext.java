package com.omega.jobservice.jobconfig;

import com.omega.jobservice.scheduledjob.ScheduleInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ScheduledJobs")
@IdClass(JobContextPrimaryKey.class)  // composite key
public class JobContext {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    private Long jobServerId = 0L;
    private String executorName;
    private String timezone;
    private Long createdTime = -1L;
    private Long jobStartTime = 0L;
    private Long executionTime = -1L;
    private Long nextExecutionTime = -1L;
    private Long endExecutionTime = -1L;
    private Long transactionTimeout = -1L;

    // Period - Custom Scheduling (mention nextExecution startTime in Seconds)
    // eg. Jobs that run every 30 min
    private Boolean isPeriodic;
    public boolean isPeriodic() {
        return isPeriodic != null && isPeriodic;
    }
    private Integer period = -1;

    private Integer maxExecutionCount = -1;
    private Integer currentExecutionCount = 0;
    private Integer jobExecutionErrorCount = 0;
    private ScheduleInfo scheduleInfo;
    private Integer loggerLevel = -1;

    @Transient
    private JobStatus statusEnum = JobStatus.CREATED;

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
    }
}
