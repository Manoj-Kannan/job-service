package com.omega.jobservice.queue.dao;

import com.omega.jobservice.queue.context.InstantJobsQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InstantJobsQueueDAO extends JpaRepository<InstantJobsQueue, Long> {
    @Query("SELECT iq FROM InstantJobsQueue iq WHERE iq.deletedTime IS NULL ORDER BY iq.addedTime ASC")
    List<InstantJobsQueue> findAllByDeletedTimeIsNullOrderByAddedTimeAsc(Pageable pageable);
}
