package com.omega.jobservice.queue.context;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "QueueData")
public class QueueData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long fileId = -1;

    private Long userId = -1L;

    private String fileName;
    private String filePath;
    private Long fileSize = -1L;
    private String contentType;
    private Long createdBy = -1L;
    private Long createdTime = -1L;
    private Boolean isDeleted;
    public boolean isDeleted() {
        return isDeleted != null && isDeleted;
    }
    private Long deletedBy = -1L;
    private Long deletedTime = -1L;

    @OneToOne(mappedBy = "queueData")
    private InstantJobsQueue instantJobsQueue;
}
