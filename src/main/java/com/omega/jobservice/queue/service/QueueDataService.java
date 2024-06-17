package com.omega.jobservice.queue.service;

import com.omega.jobservice.queue.context.QueueData;
import com.omega.jobservice.queue.dao.QueueDataDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class QueueDataService {
    @Autowired
    QueueDataDAO queueDataDao;

    public QueueData addQueueData(Serializable content) {
        // TODO - Serialize Message and Add File

        QueueData queueData = new QueueData();
        queueData.setFileName("example.txt");
        queueData.setFilePath("/path/to/file/example.txt");
        queueData.setContentType("text/plain");

        queueData = queueDataDao.save(queueData);
        return queueData;
    }

    public QueueData getQueueData(long fileId) {
        // TODO - Deserialize Message from File

        return queueDataDao.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("QueueData not found - FileId - " + fileId));
    }

}
