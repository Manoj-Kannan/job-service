package com.omega.jobservice.instantjob;

import com.omega.jobservice.init.InstantJobExecutorConf;
import com.omega.jobservice.init.InstantJobConf;
import com.omega.jobservice.jobconfig.JobConfig;
import com.omega.jobservice.util.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import lombok.Getter;

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
        File instantJobXml = FileUtils.getConfFile(config.getJobFilePath());
        JAXBContext jaxbContext = JAXBContext.newInstance(InstantJobConf.class);
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
        File executorsXml = FileUtils.getConfFile(config.getExecFilePath());
        JAXBContext jaxbContext = JAXBContext.newInstance(InstantJobExecutorConf.class);
        InstantJobExecutorConf executorsConf = (InstantJobExecutorConf) jaxbContext.createUnmarshaller().unmarshal(executorsXml);

        if (executorsConf.getExecutors() != null) {
            for (InstantJobExecutorConf.Executor executor : executorsConf.getExecutors()) {
                if (StringUtils.isNotEmpty(executor.getName()) && StringUtils.isNotEmpty(executor.getTableName())) {
                    InstantJobExecutor instantJobExecutor = new InstantJobExecutor(executor.getName(), executor.getTableName(), executor.getMaxThreads(), executor.getQueueSize(), executor.getDataRetention(), executor.getPollingFrequency());
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
}
