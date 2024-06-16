package com.omega.jobservice.queue;

import com.omega.jobservice.queue.context.QueueMessage;

import java.util.List;

public interface QueueService {
    boolean push(String jobName, String message, long userId) throws Exception;

    List<QueueMessage> pull(int limit) throws Exception;

    void delete(String messageId) throws Exception;

    boolean changeTransactionTimeOut(String messageId, int transactionTimeOut) throws Exception;

    void deleteQueue(long tTime) throws Exception;
}
