package com.omega.jobservice.jobconfig;

import java.io.Serializable;
import java.util.Objects;

public class JobContextPrimaryKey implements Serializable {

    private long userId;
    private long jobId;
    private String jobName;

    public JobContextPrimaryKey() {
    }

    public JobContextPrimaryKey(long userId, long jobId, String jobName) {
        this.userId = userId;
        this.jobId = jobId;
        this.jobName = jobName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        JobContextPrimaryKey that = (JobContextPrimaryKey) o;
        return userId == that.userId &&
                jobId == that.jobId &&
                Objects.equals(jobName, that.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, jobId, jobName);
    }
}

