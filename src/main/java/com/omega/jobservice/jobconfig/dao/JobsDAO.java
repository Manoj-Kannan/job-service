package com.omega.jobservice.jobconfig.dao;

import com.omega.jobservice.jobconfig.JobContext;
import com.omega.jobservice.jobconfig.JobContextPrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

@Repository
public interface JobsDAO extends JpaRepository<JobContext, JobContextPrimaryKey> {
    public static final Logger LOGGER = LogManager.getLogger(JobsDAO.class.getName());

    @Modifying
    @Transactional
    @Query("UPDATE JobContext j SET j.status = :status, j.jobStartTime = :jobStartTime, " +
            "j.serverId = :serverId, j.executionErrorCount = :updatedExecutionErrorCount " +
            "WHERE j.userId = :userId AND j.jobId = :jobId AND j.jobName = :jobName AND j.executionErrorCount = :executionErrorCount")
    int updateStartExecution(@Param("userId") long userId,
                             @Param("jobId") long jobId,
                             @Param("jobName") String jobName,
                             @Param("status") int status,
                             @Param("jobStartTime") long jobStartTime,
                             @Param("serverId") long serverId,
                             @Param("executionErrorCount") int executionErrorCount,
                             @Param("updatedExecutionErrorCount") int updatedExecutionErrorCount);

    @Modifying
    @Transactional
    @Query("UPDATE JobContext j SET j.status = :status, j.nextExecutionTime = :nextExecutionTime, " +
            "j.currentExecutionCount = :executionCount, j.executionErrorCount = :executionErrorCount " +
            "WHERE j.userId = :userId AND j.jobId = :jobId AND j.jobName = :jobName")
    int updateNextExecutionTimeAndCount(@Param("userId") long userId,
                                        @Param("jobId") long jobId,
                                        @Param("jobName") String jobName,
                                        @Param("status") int status,
                                        @Param("nextExecutionTime") long nextExecutionTime,
                                        @Param("executionCount") int executionCount,
                                        @Param("executionErrorCount") int executionErrorCount);

    @Modifying
    @Transactional
    @Query("UPDATE JobContext j SET j.isActive = false, j.status = :status, " +
            "j.serverId = :serverId, j.executionErrorCount = :executionErrorCount " +
            "WHERE j.userId = :userId AND j.jobId = :jobId AND j.jobName = :jobName")
    int setInActive(@Param("userId") long userId,
                    @Param("jobId") long jobId,
                    @Param("jobName") String jobName,
                    @Param("status") int status,
                    @Param("serverId") long serverId,
                    @Param("executionErrorCount") int executionErrorCount);

    @Modifying
    @Transactional
    @Query("UPDATE JobContext j SET j.isActive = false, j.status = :status, " +
            "j.serverId = :serverId, j.currentExecutionCount = :executionCount, j.executionErrorCount = :executionErrorCount " +
            "WHERE j.userId = :userId AND j.jobId = :jobId AND j.jobName = :jobName")
    int setInActiveAndUpdateCount(@Param("userId") long userId,
                                  @Param("jobId") long jobId,
                                  @Param("jobName") String jobName,
                                  @Param("status") int status,
                                  @Param("serverId") long serverId,
                                  @Param("executionCount") int executionCount,
                                  @Param("executionErrorCount") int executionErrorCount);

    JobContext findByUserIdAndJobIdAndJobName(long userId, long jobId, String jobName);

    @Query("SELECT j FROM JobContext j WHERE j.executorName = :executorName AND j.isActive = true AND j.status = :status " +
            "AND j.nextExecutionTime < :endTime AND j.currentExecutionCount < :maxRetry")
    List<JobContext> getJobs(@Param("executorName") String executorName,
                             @Param("status") int status,
                             @Param("endTime") long endTime,
                             @Param("maxRetry") int maxRetry);

    @Query("SELECT j FROM JobContext j WHERE j.executorName = :executorName AND j.isActive = true AND j.status = :status " +
            "AND j.nextExecutionTime < :endTime AND j.executionErrorCount < :maxRetry " +
            "AND (j.jobStartTime + j.transactionTimeout) < :currentTime")
    List<JobContext> getInCompletedJobs(@Param("executorName") String executorName,
                                        @Param("status") int status,
                                        @Param("endTime") long endTime,
                                        @Param("maxRetry") int maxRetry,
                                        @Param("currentTime") long currentTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM JobContext j WHERE j.userId = :userId AND j.jobId = :jobId AND j.jobName = :jobName")
    void deleteJob(@Param("userId") long userId,
                   @Param("jobId") long jobId,
                   @Param("jobName") String jobName);

    @Modifying
    @Transactional
    @Query("DELETE FROM JobContext j WHERE j.userId = :userId AND j.jobId IN :jobIds AND j.jobName = :jobName")
    int deleteJobs(@Param("userId") long userId,
                   @Param("jobIds") List<Long> jobIds,
                   @Param("jobName") String jobName);

}
