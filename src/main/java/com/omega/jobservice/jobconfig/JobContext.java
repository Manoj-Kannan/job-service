package com.omega.jobservice.jobconfig;

import com.omega.jobservice.scheduledjob.ScheduleInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobContext {
    private long jobId = -1;
    private long userId = -1;
    private long jobServerId = 0L;
    private long jobStartTime = 0L;
    private long createdTime = -1L;
    private long executionTime = -1;
    private long nextExecutionTime = -1;
    private long endExecutionTime = -1;
    private long transactionTimeout = -1;
    private boolean isActive;
    // Period - Custom Scheduling (mention nextExecution startTime in Seconds)
    private boolean isPeriodic;
    private int period = -1;
    private int maxExecutionCount = -1;
    private int currentExecutionCount = 0;
    private int jobExecutionErrorCount = 0;
    private int loggerLevel = -1;
    private String jobName;
    private String timezone;
    private String executorName;
    private ScheduleInfo scheduleInfo;
    private JobStatus statusEnum = JobStatus.CREATED;
    private int status = JobStatus.CREATED.getIndex();

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
