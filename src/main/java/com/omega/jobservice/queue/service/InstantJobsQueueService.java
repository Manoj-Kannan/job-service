package com.omega.jobservice.queue.service;

import com.omega.jobservice.queue.context.QueueMessage;
import com.omega.jobservice.queue.dao.InstantJobsQueueDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.omega.jobservice.queue.context.InstantJobsQueue;
import org.apache.commons.collections4.CollectionUtils;
import com.omega.jobservice.queue.context.QueueData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Service
public class InstantJobsQueueService {
    public final Logger LOGGER = LogManager.getLogger(InstantJobsQueueService.class.getName());

    @Autowired
    QueueDataService queueDataService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    InstantJobsQueueDAO instantJobsQueueDao;

    public boolean sendMessage(String jobName, Serializable serializableMessage) {
        QueueData queueData = queueDataService.addQueueData(serializableMessage);

        InstantJobsQueue instantJobsQueue = new InstantJobsQueue();
        instantJobsQueue.setJobName(jobName);
        instantJobsQueue.setTransactionTimeout(3600);
        instantJobsQueue.setFileId(queueData.getFileId());

        instantJobsQueueDao.save(instantJobsQueue);
        return true;
    }

    public List<QueueMessage> getMessageObjects(int limit) {
        /*
        JPQL does not have direct support for LIMIT. It can be achieved using 2 ways,
        -> setMaxResults in JPQL
        -> Pageable object param
         */
        Pageable pageable = PageRequest.of(0, limit);
        List<InstantJobsQueue> queueList = instantJobsQueueDao.findAllByDeletedTimeIsNullOrderByAddedTimeAsc(pageable);

        List<QueueMessage> queueMessageList = null;
        if (CollectionUtils.isNotEmpty(queueList)) {
            queueMessageList = new ArrayList<>();

            // Ensure queueData is fetched
            for (InstantJobsQueue instantJob : queueList) {
                QueueData queueData = instantJob.getQueueData();
                if (queueData == null) {
                    queueData = queueDataService.getQueueData(instantJob.getFileId());
                    instantJob.setQueueData(queueData);
                }

                QueueMessage queueMessage = new QueueMessage(String.valueOf(instantJob.getId()), null);
                queueMessage.setTransactionTimeOut(instantJob.getTransactionTimeout());
                queueMessage.setFileId(queueData.getFileId());
                queueMessageList.add(queueMessage);
            }
        }

        return queueMessageList;
    }

    @Transactional
    public boolean changeTransactionTimeout(String messageId, int transactionTimeout) {
        long entityId = Long.parseLong(messageId);

        InstantJobsQueue queue = instantJobsQueueDao.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("InstantJob not found - Id - : " + messageId));

        queue.setTransactionTimeout(transactionTimeout);
        instantJobsQueueDao.save(queue);
        return true;
    }

    @Transactional
    public void deleteMessageObject(String messageId) {
        long entityId = Long.parseLong(messageId);
        Optional<InstantJobsQueue> optionalQueue = instantJobsQueueDao.findById(entityId);

        if (optionalQueue.isPresent()) {
            InstantJobsQueue queue = optionalQueue.get();
            queue.setDeletedTime(System.currentTimeMillis());
            instantJobsQueueDao.save(queue);
        } else {
            throw new IllegalArgumentException("InstantJob not found - Id - : " + messageId);
        }
    }

    @Transactional
    public void deleteQueue(long tTime) {
        String queryString = "DELETE FROM InstantJobsQueue iq WHERE iq.deletedTime IS NOT NULL AND iq.deletedTime < :tTime";

        Query query = entityManager.createQuery(queryString);
        query.setParameter("tTime", tTime);
        int deletedCount = query.executeUpdate();

        LOGGER.info("Deleted " + deletedCount + " records from InstantJobsQueue.");
    }
}