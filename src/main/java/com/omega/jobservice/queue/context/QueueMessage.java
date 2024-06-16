package com.omega.jobservice.queue.context;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueueMessage {
    private long fileId;
    private final String id;
    private long transactionTimeOut;
    private String serializedMessage;
    private Object deserializedMessage;

    public QueueMessage(String id, String message) {
        this.id = id;
        this.serializedMessage = message;
    }
}
