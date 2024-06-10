package com.omega.jobservice.jobconfig;

import com.omega.jobservice.scheduledjob.ScheduledJob;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Future;

@Getter
@Setter
public class JobTimeOutContext {
    private Future future;
    private long timeOut = -1;
    private long executionTime = -1;
    private ScheduledJob scheduledJob;

    public JobTimeOutContext(long executionTime, long timeOut, Future future, ScheduledJob scheduledJob) {
        this.future = future;
        this.timeOut = timeOut;
        this.executionTime = executionTime;
        this.scheduledJob = scheduledJob;
    }
}
