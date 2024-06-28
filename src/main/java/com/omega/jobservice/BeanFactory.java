package com.omega.jobservice;

import com.omega.jobservice.scheduledjob.ScheduledJobExecutor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import com.omega.jobservice.jobconfig.service.JobsService;

@Configuration
public class BeanFactory implements ApplicationContextAware {
    private JobsService jobsService;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public BeanFactory(JobsService jobsService) {
        this.jobsService = jobsService;
    }

    public ScheduledJobExecutor createScheduledExecutor(String name, int noOfThreads, int bufferPeriod, int maxRetry) {
        return new ScheduledJobExecutor(name, noOfThreads, bufferPeriod, maxRetry, jobsService, applicationContext);
    }
}
