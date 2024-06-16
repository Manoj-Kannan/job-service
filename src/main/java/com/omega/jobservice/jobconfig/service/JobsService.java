package com.omega.jobservice.jobconfig.service;

import com.omega.jobservice.jobconfig.JobContext;
import com.omega.jobservice.jobconfig.dao.JobsDAO;
import com.omega.jobservice.scheduledjob.ScheduledJobController;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.io.IOException;
import java.util.List;

@Service
public class JobsService {
    @Autowired
    JobsDAO jobsDao;

    public long addJob(JobContext jobContext) throws Exception {
        validateJobContext(jobContext);
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

        if (jobContext.getExecutionTime() == -1) {
            throw new IllegalArgumentException("Invalid execution time for Job : " + jobContext.getJobName());
        }

        if (jobContext.isPeriodic() && jobContext.getPeriod() == -1 && jobContext.getScheduleInfo() == null) {
            throw new IllegalArgumentException("Either period or schedule info should be specified for recurring job : " + jobContext);
        }
    }

    @Transactional
    public int updateStartExecution(long userId, long jobId, String jobName, long jobStartTime, int jobExecutionCount) {
        return jobsDao.updateStartExecution(userId, jobId, jobName, jobStartTime, jobExecutionCount);
    }

    @Transactional
    public int updateNextExecutionTimeAndCount(long userId, long jobId, String jobName, long nextExecutionTime, int executionCount) throws SQLException {
        int updatedRows = jobsDao.updateNextExecutionTimeAndCount(userId, jobId, jobName, nextExecutionTime, executionCount);
        if (updatedRows == 0) {
            throw new SQLException("No rows updated. Job not found or data unchanged.");
        }
        return updatedRows;
    }

    @Transactional
    public int setInActive(long userId, long jobId, String jobName) throws SQLException {
        return jobsDao.setInActive(userId, jobId, jobName);
    }

    @Transactional
    public int setInActiveAndUpdateCount(long userId, long jobId, String jobName, int executionCount) throws SQLException {
        return jobsDao.setInActiveAndUpdateCount(userId, jobId, jobName, executionCount);
    }

    public List<JobContext> getJobs(String executorName, long startTime, long endTime, int maxRetry) throws Exception {
        return jobsDao.getJobs(executorName, startTime, endTime, maxRetry);
    }

    public List<JobContext> getInCompletedJobs(String executorName, long startTime, long endTime, int maxRetry) throws Exception {
        return jobsDao.getInCompletedJobs(executorName, startTime, endTime, maxRetry);
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
