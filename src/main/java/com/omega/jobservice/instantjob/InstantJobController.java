package com.omega.jobservice.instantjob;

import com.omega.jobservice.init.InstantJobExecutorConf;
import com.omega.jobservice.init.InstantJobConf;
import com.omega.jobservice.jobconfig.JobConfig;
import com.omega.jobservice.util.FileUtils;
import com.omega.jobservice.util.JobConstants;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import lombok.Getter;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

public class InstantJobController {
    private static final Logger LOGGER = LogManager.getLogger(InstantJobController.class.getName());

    private static final Map<String, InstantJobConf.JobConf> JOBS_MAP = new HashMap<>();
    private static final Map<String, InstantJobExecutor> EXECUTORS = new HashMap<>();
    @Getter
    private static JobConfig config = null;

    public static synchronized void initScheduler(JobConfig jobConfig) throws JAXBException {
        config = jobConfig;

        parseJobConfObject();
        if (config.isEnabledService()) {
            parseExecutorConfObject();
        }

        LOGGER.info("Scheduler Jobs : " + JOBS_MAP);
        LOGGER.info("Scheduler Executors : " + EXECUTORS);
    }

    private static void parseJobConfObject() throws JAXBException {
        ClassLoader classLoader = InstantJobController.class.getClassLoader();
        File instantJobXml = new File(classLoader.getResource(config.getJobFilePath()).getFile());

        JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[]{InstantJobConf.class}, null);
        InstantJobConf instantJobConf = (InstantJobConf) jaxbContext.createUnmarshaller().unmarshal(instantJobXml);

        if (instantJobConf.getJobs() != null) {
            for (InstantJobConf.JobConf jobConf : instantJobConf.getJobs()) {
                String name = jobConf.getName();
                if (name != null && !name.isEmpty() && jobConf.getClassObject() != null) {
                    JOBS_MAP.put(name, jobConf);
                } else {
                    LOGGER.error("Invalid job configuration : " + jobConf);
                }
            }
        }
    }

    private static void parseExecutorConfObject() throws JAXBException {
        ClassLoader classLoader = InstantJobController.class.getClassLoader();
        File executorsXml = new File(classLoader.getResource(config.getExecFilePath()).getFile());

        JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[]{InstantJobExecutorConf.class}, null);
        InstantJobExecutorConf executorsConf = (InstantJobExecutorConf) jaxbContext.createUnmarshaller().unmarshal(executorsXml);

        if (executorsConf.getExecutors() != null) {
            for (InstantJobExecutorConf.Executor executor : executorsConf.getExecutors()) {
                if (StringUtils.isNotEmpty(executor.getName())) {
                    InstantJobExecutor instantJobExecutor = new InstantJobExecutor(executor.getName(), executor.getMaxThreads(), executor.getQueueSize(), executor.getDataRetention(), executor.getPollingFrequency());
                    EXECUTORS.put(executor.getName(), instantJobExecutor);
                }
            }
        }
    }

    private static void startExecutors() {
        if (config.isEnabledService()) {
            EXECUTORS.forEach((k, v) -> {
                v.startExecutor();
            });
        }
    }

    public static void stopExecutors() {
        EXECUTORS.values().forEach(InstantJobExecutor::stopExecutor);
    }

    public static void deleteExecutorsInstantJobQueueTable() throws Exception {
        for (InstantJobExecutor executor : EXECUTORS.values()) {
            executor.deleteInstantJobQueueTable();
        }
    }

    public static InstantJobConf.JobConf getInstantJob(String jobName) {
        return JOBS_MAP.get(jobName);
    }

    public static void addInstantJob(String executorName, String jobName, Context context) throws Exception {
        if (StringUtils.isEmpty(executorName)) {
            throw new IllegalArgumentException("Executor name cannot be null while adding instant job");
        }

        InstantJobExecutor jobExec = EXECUTORS.get(executorName);
        if (jobExec == null) {
            throw new IllegalArgumentException("No such Instant job executor with name : " + executorName);
        }

        if (StringUtils.isEmpty(jobName)) {
            throw new IllegalArgumentException("Job name cannot be null while adding instant job");
        }

        if (getInstantJob(jobName) == null) {
            throw new IllegalArgumentException("No such Instant job with name : " + jobName);
        }

        context.put(JobConstants.INSTANT_JOB, jobName);
        jobExec.addJob(jobName, context);
    }
}
