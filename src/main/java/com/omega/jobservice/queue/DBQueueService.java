package com.omega.jobservice.queue;

import java.util.List;

public class DBQueueService implements QueueService {
    private String tableName;

    public DBQueueService(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public boolean push(String jobName, String message, long userId) throws Exception {
        return false;
    }

    @Override
    public List<QueueMessage> pull(int limit) throws Exception {
        return List.of();
    }

    @Override
    public void delete(String messageId) throws Exception {

    }

    @Override
    public boolean changeTransactionTimeOut(String messageId, int transactionTimeOut) throws Exception {
        return false;
    }

    @Override
    public void deleteQueue(long tTime) throws Exception {

    }
}
