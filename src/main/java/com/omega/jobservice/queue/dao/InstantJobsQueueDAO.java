package com.omega.jobservice.queue.dao;

import com.omega.jobservice.queue.context.InstantJobsQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface InstantJobsQueueDAO extends JpaRepository<InstantJobsQueue, Long> {

}
