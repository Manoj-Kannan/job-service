package com.omega.jobservice.instantjob;

import com.omega.jobservice.commands.ChainFactory;
import com.omega.jobservice.jobconfig.JobContext;
import com.omega.jobservice.util.JobConstants;
import com.omega.jobservice.util.TimeUtil;
import org.apache.commons.chain.Context;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import lombok.Getter;
import lombok.Setter;

import java.text.MessageFormat;

@Getter
@Setter
public abstract class InstantJob {
    private static final Logger LOGGER = LogManager.getLogger(InstantJob.class.getName());

    private String messageId;
    private InstantJobExecutor executor = null;

    public abstract void execute(Context context) throws Exception;

    public void _execute(Context context, long transactionTimeout) {
        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        currentThread.setName(MessageFormat.format("{0}-{1}-instant-job-{2}",
                threadName,
                executor.getName(),
                getMessageId()));

        String jobName = (String) context.remove(JobConstants.INSTANT_JOB_NAME);

        long startTime = TimeUtil.currentTimeInMillis();
        JobContext.JobStatus jobStatus = JobContext.JobStatus.CREATED;

        try {
            // TODO - Set Current Account, TimeZone, LoggerLevel

            context.put(JobConstants.INSTANT_JOB, this);
            ChainFactory.instantJobExecutionChain(transactionTimeout).execute(context);

            jobStatus = JobContext.JobStatus.COMPLETED;
        } catch (Exception e) {
            jobStatus = JobContext.JobStatus.ERROR_OCCURRED;
            LOGGER.error("Job execution failed for Job : " + jobName, e);
        } finally {
            JobContext job = new JobContext();
            job.setJobName(jobName);
            job.setIsPeriodic(false);
            job.setExecutorName("instant");

            InstantJobController.getConfig().log(job, TimeUtil.calculateMilliSecDifference(startTime), jobStatus);

            currentThread.setName(threadName);
            executor.endJob(getMessageId());
        }
    }

    public void handleTimeOut() {

    }
}
