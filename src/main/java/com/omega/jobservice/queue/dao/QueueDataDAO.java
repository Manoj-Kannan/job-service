package com.omega.jobservice.queue.dao;

import com.omega.jobservice.queue.context.QueueData;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Repository
public interface QueueDataDAO extends JpaRepository<QueueData, Long> {
    public static final Logger LOGGER = LogManager.getLogger(QueueDataDAO.class.getName());

}
