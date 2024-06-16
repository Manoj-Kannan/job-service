package com.omega.jobservice.queue.service;

import com.omega.jobservice.queue.context.QueueMessage;
import com.omega.jobservice.queue.dao.InstantJobsQueueDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
public class InstantJobsQueueService {
    @Autowired
    InstantJobsQueueDAO instantJobsQueueDao;

    public boolean sendMessage(String jobName, Serializable serializableMessage) throws Exception {
        return false;
    }

    public List<QueueMessage> getMessageObjects(int limit) throws Exception {
        return List.of();
    }

    public boolean changeTransactionTimeout(String id, int transactionTimeout) throws Exception {
        return false;
    }

    public void deleteMessageObject(String receiptHandle) throws Exception {

    }

    public void deleteQueue(long tTime) throws Exception {

    }
}