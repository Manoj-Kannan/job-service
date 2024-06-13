package com.omega.jobservice.jobconfig;

import com.omega.jobservice.scheduledjob.ScheduledJob;
import com.omega.jobservice.instantjob.InstantJob;
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
    private InstantJob instantJob;

    public JobTimeOutContext(long executionTime, long timeOut, Future future, ScheduledJob scheduledJob) {
        this.future = future;
        this.timeOut = timeOut;
        this.executionTime = executionTime;
        this.scheduledJob = scheduledJob;
    }

    public JobTimeOutContext(long executionTime, long timeOut, Future future, InstantJob instantJob) {
        this.future = future;
        this.timeOut = timeOut;
        this.executionTime = executionTime;
        this.instantJob = instantJob;
    }
}
