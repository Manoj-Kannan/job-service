package com.omega.jobservice.queue.context;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "InstantJobsQueue")
public class InstantJobsQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = -1;

    private Long userId = -1L;

    @Column(nullable = false)
    private String jobName;

    // to ensure that fileId value is managed by the relationship and not directly inserted or updated
    @Column(nullable = false, insertable = false, updatable = false)
    private long fileId = -1;

    @Column(nullable = false)
    private long transactionTimeout = -1;

    private Long addedTime = -1L;
    private Long deletedTime = -1L;
    private Integer maxExecutionCount = -1;
    private Integer currentExecutionCount = 0;
    private Integer clientReceivedTime = 0;

    @OneToOne
    @JoinColumn(name = "fileId", referencedColumnName = "fileId")
    private QueueData queueData;
}
