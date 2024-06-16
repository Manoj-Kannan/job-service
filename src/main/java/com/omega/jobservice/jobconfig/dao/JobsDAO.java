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
    @Query("UPDATE JobContext j SET j.jobStartTime = :jobStartTime, j.currentExecutionCount = :jobExecutionCount " +
            "WHERE j.userId = :userId AND j.jobId = :jobId AND j.jobName = :jobName")
    int updateStartExecution(@Param("userId") long userId,
                             @Param("jobId") long jobId,
                             @Param("jobName") String jobName,
                             @Param("jobStartTime") long jobStartTime,
                             @Param("jobExecutionCount") int jobExecutionCount);

    @Modifying
    @Transactional
    @Query("UPDATE JobContext j SET j.nextExecutionTime = :nextExecutionTime, j.currentExecutionCount = :executionCount " +
            "WHERE j.userId = :userId AND j.jobId = :jobId AND j.jobName = :jobName")
    int updateNextExecutionTimeAndCount(@Param("userId") long userId,
                                        @Param("jobId") long jobId,
                                        @Param("jobName") String jobName,
                                        @Param("nextExecutionTime") long nextExecutionTime,
                                        @Param("executionCount") int executionCount);

    @Modifying
    @Transactional
    @Query("UPDATE JobContext j SET j.isActive = false " +
            "WHERE j.userId = :userId AND j.jobId = :jobId AND j.jobName = :jobName")
    int setInActive(@Param("userId") long userId,
                    @Param("jobId") long jobId,
                    @Param("jobName") String jobName);

    @Modifying
    @Transactional
    @Query("UPDATE JobContext j SET j.isActive = false, j.currentExecutionCount = :executionCount " +
            "WHERE j.userId = :userId AND j.jobId = :jobId AND j.jobName = :jobName")
    int setInActiveAndUpdateCount(@Param("userId") long userId,
                                  @Param("jobId") long jobId,
                                  @Param("jobName") String jobName,
                                  @Param("executionCount") int executionCount);

    @Query("SELECT j FROM JobContext j WHERE j.executorName = :executorName AND j.jobStartTime >= :startTime AND j.jobStartTime <= :endTime AND j.currentExecutionCount <= :maxRetry")
    List<JobContext> getJobs(@Param("executorName") String executorName,
                             @Param("startTime") long startTime,
                             @Param("endTime") long endTime,
                             @Param("maxRetry") int maxRetry);

    @Query("SELECT j FROM JobContext j WHERE j.executorName = :executorName AND j.jobStartTime >= :startTime AND j.jobStartTime <= :endTime AND j.currentExecutionCount <= :maxRetry AND j.status != 3")
    List<JobContext> getInCompletedJobs(@Param("executorName") String executorName,
                                        @Param("startTime") long startTime,
                                        @Param("endTime") long endTime,
                                        @Param("maxRetry") int maxRetry);

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
