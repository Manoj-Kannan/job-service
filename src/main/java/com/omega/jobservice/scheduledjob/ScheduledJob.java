package com.omega.jobservice.scheduledjob;

import com.omega.jobservice.jobconfig.service.JobsService;
import org.springframework.context.ApplicationContext;
import com.omega.jobservice.commands.ChainFactory;
import com.omega.jobservice.jobconfig.JobContext;
import com.omega.jobservice.util.JobConstants;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.Context;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import lombok.Getter;
import lombok.Setter;

import java.sql.SQLException;
import java.time.Instant;

@Getter
@Setter
public abstract class ScheduledJob implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(ScheduledJob.class.getName());

    private ApplicationContext applicationContext;
    private JobContext jobContext = null;
    private ScheduledJobExecutor executor;
    private int retryExecutionCount = 1;
    private JobsService jobService;

    public abstract void execute(JobContext jobContext) throws Exception;

    @Override
    public void run() {
        jobService.updateStartExecution(jobContext.getUserId(), jobContext.getJobId(), jobContext.getJobName(), jobContext.getJobStartTime(), jobContext.getExecutionErrorCount());

        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        currentThread.setName(threadName + "-" + jobContext.getJobId() + "-" + jobContext.getJobName());

        long startTime = 0L;
        JobContext.JobStatus jobStatus = JobContext.JobStatus.CREATED;

        try {
            retryExecutionCount++;
            startTime = System.currentTimeMillis();
            LOGGER.debug("Starting job " + jobContext.getJobKey());

            // TODO - Set Current Account, TimeZone, LoggerLevel

            jobContext.setNextExecutionTime(getNextExecutionTime());

            Context context = new ContextBase();
            context.put(JobConstants.JOB_CONTEXT, jobContext);
            context.put(JobConstants.SCHEDULED_JOB, this);

            ChainBase jobExecutionChain = ChainFactory.scheduledJobExecutionChain(jobContext.getTransactionTimeout());
            jobExecutionChain.execute(context);

            executor.endJob(jobContext.getJobKey());
            jobStatus = JobContext.JobStatus.COMPLETED;
        } catch (Exception e) {
            jobStatus = JobContext.JobStatus.ERROR_OCCURRED;
            LOGGER.error("Job execution failed for Job :" + jobContext.getJobId() + " : " + jobContext.getJobName(), e);

            // TODO - Reschedule
        } finally {
            long timeTaken = (System.currentTimeMillis() - startTime);
            ScheduledJobController.getConfig().log(jobContext, timeTaken, jobStatus);

            if (jobStatus.equals(JobContext.JobStatus.COMPLETED)) {
                updateNextExecutionTime();
            }
            LOGGER.debug("Job completed " + jobContext.getJobId() + "-" + jobContext.getJobName() + " time taken : " + timeTaken);
        }
    }

    private long getNextExecutionTime() {
        long nextExecutionTime = -1;
        if (jobContext.isPeriodic() && (jobContext.getMaxExecutionCount() == -1 || (jobContext.getCurrentExecutionCount() + 1 < jobContext.getMaxExecutionCount()))) {
            if (jobContext.getPeriod() != -1) {
                nextExecutionTime = (Instant.now().getEpochSecond()) + jobContext.getPeriod();
            } else if (jobContext.getScheduleInfo() != null) {
                nextExecutionTime = jobContext.getScheduleInfo().nextExecutionTime(jobContext.getNextExecutionTime());

                if (nextExecutionTime == jobContext.getNextExecutionTime()) {
                    // One time job
                    return -1;
                }

                while (nextExecutionTime <= Instant.now().getEpochSecond()) {
                    nextExecutionTime = jobContext.getScheduleInfo().nextExecutionTime(nextExecutionTime);
                }
            }
            if (jobContext.getEndExecutionTime() == -1 || nextExecutionTime <= jobContext.getEndExecutionTime()) {
                return nextExecutionTime;
            }
        }

        return nextExecutionTime;
    }

    private void updateNextExecutionTime() {
        try {
            if (jobContext.getNextExecutionTime() != -1) {
                jobService.updateNextExecutionTimeAndCount(jobContext.getUserId(), jobContext.getJobId(), jobContext.getJobName(), jobContext.getNextExecutionTime(), jobContext.getCurrentExecutionCount() + 1);
            } else {
                jobService.setInActiveAndUpdateCount(jobContext.getUserId(), jobContext.getJobId(), jobContext.getJobName(), jobContext.getCurrentExecutionCount() + 1);
            }
        } catch (Exception e) {
            LOGGER.error("Exception while updating next execution time ", e);
        }
    }

    private void reschedule() throws SQLException {
        if (retryExecutionCount <= executor.getMaxRetry()) {
            LOGGER.error("Rescheduling : " + jobContext.getJobId() + "::" + jobContext.getJobName() + " for the " + retryExecutionCount + " time.");
            executor.reSchedule(jobContext, this);
        } else {
            jobService.setInActive(jobContext.getUserId(), jobContext.getJobId(), jobContext.getJobName());
            LOGGER.error("Max retry exceeded for : " + jobContext + ".\nSo making it inactive");
            ScheduledJobController.getConfig().emailException("ScheduledJobContext", "Max retry exceeded for Job : " + jobContext.getJobId() + " : " + jobContext.getJobName(), "\nSince max retries exceeded for job : " + jobContext.getJobId() + "-" + jobContext.getJobName() + ", making it inactive.");
        }
    }

    public void handleTimeOut() {

    }
}
