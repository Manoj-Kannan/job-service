package com.omega.jobservice.jobconfig;

public interface JobConfig {
    String getJobFilePath();

    String getExecFilePath();

    void emailException(String fromClass, String msg, Throwable t);

    void emailException(String fromClass, String msg, String reason);

    void log(JobContext jobContext, long timeTaken, JobContext.JobStatus status);
}
