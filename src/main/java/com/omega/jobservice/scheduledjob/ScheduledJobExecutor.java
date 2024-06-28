package com.omega.jobservice.scheduledjob;

import org.apache.commons.collections4.CollectionUtils;
import com.omega.jobservice.jobconfig.service.JobsService;
import org.springframework.context.ApplicationContext;
import com.omega.jobservice.init.ScheduledJobConf;
import com.omega.jobservice.jobconfig.JobTimeOutContext;
import com.omega.jobservice.jobconfig.JobContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Getter
public class ScheduledJobExecutor implements Runnable {
    private static final int MAX_RETRY = 5;
    private static final int JOB_TIMEOUT_BUFFER = 0;
    private static final Logger LOGGER = LogManager.getLogger(ScheduledJobExecutor.class.getName());

    // managing the instantiation and dependency injection dynamically (explicit control over how instances are created and injected)
    private JobsService jobService;
    private ApplicationContext applicationContext;

    private String name = null;
    private int bufferPeriod;
    private int maxRetry = MAX_RETRY;
    private ScheduledExecutorService executor = null;
    private final ConcurrentMap<String, JobTimeOutContext> jobMonitor = new ConcurrentHashMap<>();

    public ScheduledJobExecutor(String name, int noOfThreads, int bufferPeriod, int maxRetry, JobsService jobService, ApplicationContext applicationContext) {
        this.name = name;
        this.jobService = jobService;
        this.bufferPeriod = bufferPeriod;
        if (maxRetry > 0) {
            this.maxRetry = maxRetry;
        }
        this.applicationContext = applicationContext;
        executor = Executors.newScheduledThreadPool(noOfThreads + 1);
        executor.scheduleAtFixedRate(this, 0, bufferPeriod * 1000L, TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return "ScheduledJobExecutor{name, bufferPeriod, maxRetry}={" + name + "," + bufferPeriod + "," + maxRetry + "}";
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        currentThread.setName("Executor-" + this.name);

        try {
            // TODO - clean current user & set account
            handleTimeOut();

            long startTime = System.currentTimeMillis() / 1000;
            long endTime = startTime + bufferPeriod;

            List<JobContext> jobs = jobService.getJobs(name, endTime, getMaxRetry());
            List<JobContext> inCompletedJobs = jobService.getInCompletedJobs(name, endTime, getMaxRetry());

            jobs = CollectionUtils.isEmpty(jobs) ? new ArrayList<>() : jobs;
            if (CollectionUtils.isNotEmpty(inCompletedJobs)) {
                jobs.addAll(inCompletedJobs);
            }

            for (JobContext currJob : jobs) {
                try {
                    scheduleJob(currJob);
                } catch (Exception e) {
                    LOGGER.error("Unable to schedule job : " + currJob);
                    LOGGER.error("Exception occurred ", e);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Exception occurred ", e);
            ScheduledJobController.getConfig().emailException("Executor : ", this.name + " is Down", e);
        } finally {
            currentThread.setName(threadName);
        }
    }

    public void scheduleJob(JobContext dbJobContext) throws Exception {
        ScheduledJobConf.JobConf jobConf = ScheduledJobController.getScheduledJobConf(dbJobContext.getJobName());
        if (jobConf != null) {
            Class<? extends ScheduledJob> classObject = jobConf.getClassObject();
            if (classObject != null) {
                ScheduledJob scheduledJob = jobConf.getClassObject().newInstance();
                scheduledJob.setApplicationContext(applicationContext);
                scheduledJob.setJobContext(dbJobContext);
                scheduledJob.setJobService(jobService);
                scheduledJob.setExecutor(this);

                LOGGER.debug("Scheduling Job : " + dbJobContext);
                schedule(dbJobContext, scheduledJob);
            }
        }
    }

    private void schedule(JobContext dbJobContext, ScheduledJob scheduledJob) {
        long delay = (dbJobContext.getNextExecutionTime() - (System.currentTimeMillis() / 1000));
        Future future = executor.schedule(scheduledJob, delay, TimeUnit.SECONDS);

        // Jobs that has execution time in the past, are considered for immediate execution
        if (delay > 0) {
            jobMonitor.put(dbJobContext.getJobKey(),
                    new JobTimeOutContext(dbJobContext.getNextExecutionTime() * 1000, (dbJobContext.getTransactionTimeout() + JOB_TIMEOUT_BUFFER), future, scheduledJob));
        } else {
            jobMonitor.put(dbJobContext.getJobKey(),
                    new JobTimeOutContext(System.currentTimeMillis() + 1000, (dbJobContext.getTransactionTimeout() + JOB_TIMEOUT_BUFFER), future, scheduledJob));
        }
    }

    public void reSchedule(JobContext dbJobContext, ScheduledJob scheduledJob) {
        endJob(dbJobContext.getJobKey());
        Future f = executor.schedule(scheduledJob, 1, TimeUnit.SECONDS);
        jobMonitor.put(dbJobContext.getJobKey(),
                new JobTimeOutContext(System.currentTimeMillis() + 1000, (dbJobContext.getTransactionTimeout() + JOB_TIMEOUT_BUFFER), f, scheduledJob));
    }

    public void endJob(String jobKey) {
        jobMonitor.remove(jobKey);
    }


    public void shutdownExecutor() {
        executor.shutdownNow();
    }

    private void handleTimeOut() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, JobTimeOutContext>> itr = jobMonitor.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, JobTimeOutContext> entry = itr.next();
            JobTimeOutContext info = entry.getValue();
            if (currentTime >= (info.getExecutionTime() + info.getTimeOut())) {
                if (info.getFuture().cancel(true)) {
                    LOGGER.info("Time exceeded for job : " + entry.getKey());
                    info.getScheduledJob().handleTimeOut();
                    itr.remove();
                }
            }
        }
    }
}
