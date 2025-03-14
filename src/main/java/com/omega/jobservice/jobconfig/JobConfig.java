package com.omega.jobservice.jobconfig;

public interface JobConfig {
    long getServerId();

    String getJobFilePath();

    String getExecFilePath();

    boolean isEnabledService();

    void emailException(String fromClass, String msg, Throwable t);

    void emailException(String fromClass, String msg, String reason);

    void log(JobContext jobContext, long timeTaken, JobContext.JobStatus status);
}
