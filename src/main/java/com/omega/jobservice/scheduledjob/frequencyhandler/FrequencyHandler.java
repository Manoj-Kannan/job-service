package com.omega.jobservice.scheduledjob.frequencyhandler;

public interface FrequencyHandler {
    long nextExecutionTime(long initialExecutionTimeInMillis, long jobTimeInMillis);
}
