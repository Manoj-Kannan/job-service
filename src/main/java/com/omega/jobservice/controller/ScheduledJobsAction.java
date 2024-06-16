package com.omega.jobservice.controller;

import com.omega.jobservice.jobconfig.service.JobsService;
import com.omega.jobservice.jobconfig.JobContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/scheduledJobs")
public class ScheduledJobsAction {

    @Autowired
    private JobsService jobService;

    @PostMapping("/addJob")
    public ResponseEntity<Long> addJob(@RequestBody JobContext jobContext) throws Exception {
        long jobId = jobService.addJob(jobContext);
        return ResponseEntity.ok(jobId);
    }

    @PutMapping("/updateStartExecution")
    public ResponseEntity<Integer> updateStartExecution(@RequestParam long userId,
                                                        @RequestParam long jobId,
                                                        @RequestParam String jobName,
                                                        @RequestParam long jobStartTime,
                                                        @RequestParam int jobExecutionCount) {
        int updatedRows = jobService.updateStartExecution(userId, jobId, jobName, jobStartTime, jobExecutionCount);
        return ResponseEntity.ok(updatedRows);
    }

    @PutMapping("/updateNextExecutionTimeAndCount")
    public ResponseEntity<Integer> updateNextExecutionTimeAndCount(@RequestParam long userId,
                                                                   @RequestParam long jobId,
                                                                   @RequestParam String jobName,
                                                                   @RequestParam long nextExecutionTime,
                                                                   @RequestParam int executionCount) throws SQLException {
        int updatedRows = jobService.updateNextExecutionTimeAndCount(userId, jobId, jobName, nextExecutionTime, executionCount);
        return ResponseEntity.ok(updatedRows);
    }

    @PutMapping("/setInActive")
    public ResponseEntity<Integer> setInActive(@RequestParam long userId,
                                               @RequestParam long jobId,
                                               @RequestParam String jobName) throws SQLException {
        int updatedRows = jobService.setInActive(userId, jobId, jobName);
        return ResponseEntity.ok(updatedRows);
    }

    @PutMapping("/setInActiveAndUpdateCount")
    public ResponseEntity<Integer> setInActiveAndUpdateCount(@RequestParam long userId,
                                                             @RequestParam long jobId,
                                                             @RequestParam String jobName,
                                                             @RequestParam int executionCount) throws SQLException {
        int updatedRows = jobService.setInActiveAndUpdateCount(userId, jobId, jobName, executionCount);
        return ResponseEntity.ok(updatedRows);
    }

    @GetMapping("/getJobs")
    public ResponseEntity<List<JobContext>> getJobs(@RequestParam String executorName,
                                                    @RequestParam long startTime,
                                                    @RequestParam long endTime,
                                                    @RequestParam int maxRetry) throws Exception {
        List<JobContext> jobs = jobService.getJobs(executorName, startTime, endTime, maxRetry);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/getInCompletedJobs")
    public ResponseEntity<List<JobContext>> getInCompletedJobs(@RequestParam String executorName,
                                                               @RequestParam long startTime,
                                                               @RequestParam long endTime,
                                                               @RequestParam int maxRetry) throws Exception {
        List<JobContext> jobs = jobService.getInCompletedJobs(executorName, startTime, endTime, maxRetry);
        return ResponseEntity.ok(jobs);
    }

    @DeleteMapping("/deleteJob")
    public ResponseEntity<Void> deleteJob(@RequestParam long userId,
                                          @RequestParam long jobId,
                                          @RequestParam String jobName) throws Exception {
        jobService.deleteJob(userId, jobId, jobName);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/deleteJobs")
    public ResponseEntity<Integer> deleteJobs(@RequestParam long userId,
                                              @RequestParam List<Long> jobIds,
                                              @RequestParam String jobName) throws Exception {
        int deletedRows = jobService.deleteJobs(userId, jobIds, jobName);
        return ResponseEntity.ok(deletedRows);
    }
}

