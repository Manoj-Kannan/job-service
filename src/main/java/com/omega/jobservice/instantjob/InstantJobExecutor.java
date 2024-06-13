package com.omega.jobservice.instantjob;

import com.omega.jobservice.init.InstantJobConf;
import com.omega.jobservice.jobconfig.JobTimeOutContext;
import com.omega.jobservice.queue.ObjectQueue;
import com.omega.jobservice.queue.QueueMessage;
import com.omega.jobservice.util.JobConstants;
import org.apache.commons.chain.Context;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import lombok.Getter;

import java.util.concurrent.*;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

@Getter
public class InstantJobExecutor implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(InstantJobExecutor.class.getName());
    private static final long KEEP_ALIVE = 300000L;
    private static final int QUEUE_SIZE = 100;

    private int maxThreads;
    private final String name;
    private final String tableName;
    private final int dataRetention;
    private final int pollingFrequency;
    private boolean isRunning = false;
    private ObjectQueue objectQueue = null;
    private ThreadPoolExecutor threadPoolExecutor = null;
    private final ConcurrentMap<String, JobTimeOutContext> jobMonitorMap = new ConcurrentHashMap<>();

    public InstantJobExecutor(String name, String tableName, int maxThreads, int queueSize, int dataRetention, int pollingFrequency) {
        this.name = name;
        this.tableName = tableName;
        this.dataRetention = dataRetention;
        this.pollingFrequency = pollingFrequency;
        this.objectQueue = new ObjectQueue(tableName);
        this.threadPoolExecutor = new ThreadPoolExecutor(this.maxThreads, //core pool size
                this.maxThreads, //max pool size
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueSize > 0 ? queueSize : QUEUE_SIZE));
        this.maxThreads = maxThreads > 0 ? maxThreads : JobConstants.DEFAULT_MAX_THREADS;
    }

    @Override
    public void run() {
        LOGGER.debug("Executor Name : " + name + " Status : " + isRunning);
        while (isRunning) {
            try {
                handleTimeOut();
                int noOfThreads = getNoOfFreeThreads();
                if (noOfThreads == 0) {
                    continue;
                }
                List<QueueMessage> messageList = objectQueue.getMessageObjects(noOfThreads);
                if (messageList != null) {
                    for (QueueMessage message : messageList) {
                        Context context = (Context) message.getDeserializedMessage();
                        if (context == null) {
                            objectQueue.deleteMessageObject(message.getId());
                        }

                        String jobName = (String) context.get(JobConstants.INSTANT_JOB_NAME);
                        if (jobName == null) {
                            continue;
                        }

                        InstantJobConf.JobConf instantJob = InstantJobController.getInstantJob(jobName);
                        if (instantJob != null) {
                            Class<? extends InstantJob> jobClass = instantJob.getClassObject();
                            if (jobClass != null) {
                                try {
                                    final InstantJob job = jobClass.newInstance();
                                    if (instantJob.getTransactionTimeout() != InstantJobConf.DEFAULT_TIME_OUT) {
                                        try {
                                            objectQueue.changeTransactionTimeout(message.getId(), (int) TimeUnit.SECONDS.toMinutes(instantJob.getTransactionTimeout()));
                                        } catch (Exception e) {
                                            LOGGER.info("Ignoring job " + jobName + " since it's not available");
                                            continue;
                                        }
                                    }
                                    String receiptHandle = message.getId();
                                    job.setMessageId(receiptHandle);
                                    job.setExecutor(this);

                                    LOGGER.debug("Executing job : " + jobName);

                                    Future f = threadPoolExecutor.submit(() -> job._execute(context,
                                            (instantJob.getTransactionTimeout() - InstantJobConf.JOB_TIMEOUT_BUFFER) * 1000));

                                    jobMonitorMap.put(receiptHandle, new JobTimeOutContext(
                                            System.currentTimeMillis(),
                                            (instantJob.getTransactionTimeout() + InstantJobConf.JOB_TIMEOUT_BUFFER) * 1000L, f,
                                            job));

                                } catch (Exception e) {
                                    LOGGER.info("Ignoring job " + jobName + " since it's not available");
                                    continue;
                                }
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(pollingFrequency);
                } catch (InterruptedException e) {
                    LOGGER.info("Exception in pollingFrequencySleep ", e);
                }
            } catch (Exception e) {
                LOGGER.info("Exception in Instant Job Queue Executor : " + e);
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.debug("Executor Name : " + name + " Status : " + isRunning);
    }

    public void startExecutor() {
        if (!isRunning) {
            isRunning = true;
            new Thread(this, "instantJobExecutor").start();
        }
    }

    public void stopExecutor() {
        isRunning = false;
    }

    public void addJob(String jobName, Context context) throws Exception {
        if (!objectQueue.sendMessage(jobName, (Serializable) context)) {
            throw new IllegalArgumentException("Unable to add instant job to queue");
        }
    }

    public void endJob(String jobId) {
        try {
            objectQueue.deleteMessageObject(jobId);
        } catch (Exception e) {
            LOGGER.info("Exception occurred on deleting Instant Job :  " + e);
        }
        jobMonitorMap.remove(jobId);
    }

    private int getNoOfFreeThreads() {
        int freeCount = maxThreads - threadPoolExecutor.getActiveCount();
        LOGGER.debug("Instant Jobs - Name : " + name + ", Free Count : " + freeCount);
        return freeCount;
    }

    private void handleTimeOut() {
        Iterator<Map.Entry<String, JobTimeOutContext>> itr = jobMonitorMap.entrySet().iterator();
        long currentTime = System.currentTimeMillis();
        while (itr.hasNext()) {
            JobTimeOutContext info = itr.next().getValue();
            if (currentTime >= (info.getExecutionTime() + info.getTimeOut())) {
                if (info.getFuture().cancel(true)) {
                    info.getInstantJob().handleTimeOut();
                    itr.remove();
                }
            }
        }
    }

    public void deleteInstantJobQueueTable() throws Exception {
        long deletionTillDate = (System.currentTimeMillis() - ((long) dataRetention * 24 * 60 * 60 * 1000));

        try {
            objectQueue.deleteQueue(deletionTillDate);
        } catch (Exception e) {
            LOGGER.info("Exception occurred in InstantJob Queue Deletion :  " + tableName + " with tillDate --" + deletionTillDate, e);
        }
    }
}
