package com.omega.jobservice.queue;

import java.io.Serializable;
import java.util.List;

public class ObjectQueue {
    private String tableName;
    private DBQueueService dbQueueService;

    public ObjectQueue(String tableName) {
        this.tableName = tableName;
        dbQueueService = new DBQueueService(tableName);
    }


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