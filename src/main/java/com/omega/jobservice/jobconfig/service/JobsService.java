package com.omega.jobservice.jobconfig.service;

import com.omega.jobservice.init.ScheduledJobConf;
import com.omega.jobservice.jobconfig.JobContext;
import com.omega.jobservice.jobconfig.dao.JobsDAO;
import com.omega.jobservice.scheduledjob.ScheduledJobController;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class JobsService {
    @Autowired
    JobsDAO jobsDao;

    public long addJob(JobContext jobContext) throws Exception {
        /*
        Expected Params:
        jobName & executorName (as per JobConf declared)
        jobId (any context id), userId, nextExecutionTime (expected job start time)
        maxExecutionCount (max retry count), endExecutionTime (maximum start time)
        isPeriodic - should contain period (in min) or scheduleInfo

        Default Params:
        isActive = true (job is alive)
        status = JobStatus.CREATED (created state)
        createdTime = System.currentTimeMillis()

        Optional:
        transactionTimeout (as per JobConf declared)
        loggerLevel (based on need)

        Decide:
        jobServerId, timezone
         */

        validateJobContext(jobContext);
        setJobDefaultParams(jobContext);
        jobContext = jobsDao.save(jobContext);

        return jobContext.getJobId();
    }

    private void validateJobContext(JobContext jobContext) throws Exception {
        if (jobContext.getJobId() == -1) {
            throw new IllegalArgumentException("Job Id cannot be null");
        }

        if (jobContext.getJobName() == null || jobContext.getJobName().isEmpty()) {
            throw new IllegalArgumentException("Job name cannot be null");
        }

        if (ScheduledJobController.getScheduledJobConf(jobContext.getJobName()) == null) {
            throw new IllegalArgumentException("Scheduled Job is not configured");
        }

        if (jobContext.getExecutorName() == null || jobContext.getExecutorName().isEmpty()) {
            throw new IllegalArgumentException("Job Executor Name cannot be null. Job : " + jobContext.getJobName());
        }

        if (jobContext.getNextExecutionTime() == -1) {
            throw new IllegalArgumentException("Invalid execution time for Job : " + jobContext.getJobName());
        }

        if (jobContext.isPeriodic() && jobContext.getPeriod() == -1 && jobContext.getScheduleInfo() == null) {
            throw new IllegalArgumentException("Either period or schedule info should be specified for recurring job : " + jobContext);
        }
    }

    private void setJobDefaultParams(JobContext jobContext) {
        jobContext.setIsActive(true);
        jobContext.setCreatedTime(System.currentTimeMillis());

        if (jobContext.getTransactionTimeout() <= 0) {
            ScheduledJobConf.JobConf jobConf = ScheduledJobController.getScheduledJobConf(jobContext.getJobName());
            jobContext.setTransactionTimeout(jobConf.getTransactionTimeout());
        }
    }

    @Transactional
    public int updateStartExecution(long userId, long jobId, String jobName, long jobStartTime, int executionErrorCount) {
        int updatedExecutionErrorCount = executionErrorCount + 1;
        int status = JobContext.JobStatus.IN_PROGRESS.getIndex();
        long serverId = ScheduledJobController.getConfig().getServerId();

        return jobsDao.updateStartExecution(userId, jobId, jobName, status, jobStartTime, serverId, executionErrorCount, updatedExecutionErrorCount);
    }

    @Transactional
    public int updateNextExecutionTimeAndCount(long userId, long jobId, String jobName, long nextExecutionTime, int executionCount) throws SQLException {
        int executionErrorCount = 0;
        int status = JobContext.JobStatus.CREATED.getIndex();

        return jobsDao.updateNextExecutionTimeAndCount(userId, jobId, jobName, status, nextExecutionTime, executionCount, executionErrorCount);
    }

    @Transactional
    public int setInActive(long userId, long jobId, String jobName) throws SQLException {
        int executionErrorCount = 0;
        int status = JobContext.JobStatus.COMPLETED.getIndex();
        long serverId = ScheduledJobController.getConfig().getServerId();

        return jobsDao.setInActive(userId, jobId, jobName, status, serverId, executionErrorCount);
    }

    @Transactional
    public int setInActiveAndUpdateCount(long userId, long jobId, String jobName, int executionCount) throws SQLException {
        int executionErrorCount = 0;
        int status = JobContext.JobStatus.COMPLETED.getIndex();
        long serverId = ScheduledJobController.getConfig().getServerId();

        return jobsDao.setInActiveAndUpdateCount(userId, jobId, jobName, status, serverId, executionCount, executionErrorCount);
    }

    public JobContext getJob(long userId, long jobId, String jobName) throws Exception {
        return jobsDao.findByUserIdAndJobIdAndJobName(userId, jobId, jobName);
    }

    public List<JobContext> getJobs(String executorName, long endTime, int maxRetry) throws Exception {
        int status = JobContext.JobStatus.CREATED.getIndex();

        return jobsDao.getJobs(executorName, status, endTime, maxRetry);
    }

    public List<JobContext> getInCompletedJobs(String executorName, long endTime, int maxRetry) throws Exception {
        int status = JobContext.JobStatus.IN_PROGRESS.getIndex();

        return jobsDao.getInCompletedJobs(executorName, status, endTime, maxRetry, System.currentTimeMillis());
    }

    @Transactional
    public void deleteJob(long userId, long jobId, String jobName) throws Exception {
        jobsDao.deleteJob(userId, jobId, jobName);
    }

    @Transactional
    public int deleteJobs(long userId, List<Long> jobIds, String jobName) throws Exception {
        return jobsDao.deleteJobs(userId, jobIds, jobName);
    }
}
