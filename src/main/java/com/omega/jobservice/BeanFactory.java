package com.omega.jobservice;

import com.omega.jobservice.scheduledjob.ScheduledJobExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import com.omega.jobservice.jobconfig.service.JobsService;

@Configuration
public class BeanFactory {
    private JobsService jobsService;

    @Autowired
    public BeanFactory(JobsService jobsService) {
        this.jobsService = jobsService;
    }

    public ScheduledJobExecutor createScheduledExecutor(String name, int noOfThreads, int bufferPeriod, int maxRetry) {
        return new ScheduledJobExecutor(name, noOfThreads, bufferPeriod, maxRetry, jobsService);
    }
}
