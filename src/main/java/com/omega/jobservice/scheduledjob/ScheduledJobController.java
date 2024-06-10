package com.omega.jobservice.scheduledjob;

import com.omega.jobservice.init.ScheduledJobConf;
import com.omega.jobservice.jobconfig.JobConfig;
import com.omega.jobservice.init.ScheduledJobExecutorConf;
import org.apache.commons.lang3.StringUtils;
import com.omega.jobservice.util.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import lombok.Getter;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

public class ScheduledJobController {
    private static final Logger LOGGER = LogManager.getLogger(ScheduledJobController.class.getName());

    private static final Map<String, ScheduledJobConf.JobConf> JOBS_MAP = new HashMap<>();
    private static final List<ScheduledJobExecutor> EXECUTORS = new ArrayList<>();
    @Getter
    private static JobConfig config = null;

    public static synchronized void initScheduler(JobConfig jobConfig) throws JAXBException {
        config = jobConfig;

        parseJobConfObject();
        parseExecutorConfObject();

        LOGGER.info("Scheduler Jobs : " + JOBS_MAP);
        LOGGER.info("Scheduler Executors : " + EXECUTORS);
    }

    private static void parseJobConfObject() throws JAXBException {
        File schedulerXml = FileUtils.getConfFile(config.getJobFilePath());
        JAXBContext jaxbContext = JAXBContext.newInstance(ScheduledJobConf.class);
        ScheduledJobConf schedulerConf = (ScheduledJobConf) jaxbContext.createUnmarshaller().unmarshal(schedulerXml);

        if (schedulerConf.getJobs() != null) {
            for (ScheduledJobConf.JobConf jobConf : schedulerConf.getJobs()) {
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
        ClassLoader classLoader = ScheduledJobController.class.getClassLoader();
        File executorsXml = FileUtils.getConfFile(config.getExecFilePath());

        JAXBContext jaxbContext = JAXBContext.newInstance(ScheduledJobExecutorConf.class);
        ScheduledJobExecutorConf executorsConf = (ScheduledJobExecutorConf) jaxbContext.createUnmarshaller().unmarshal(executorsXml);

        if (executorsConf.getExecutors() != null) {
            for (ScheduledJobExecutorConf.Executor executor : executorsConf.getExecutors()) {
                if (StringUtils.isNotEmpty(executor.getName()) && executor.getPeriod() != -1 && executor.getThreads() != -1) {
                    if (executor.getMaxRetry() > 0) {
                        EXECUTORS.add(new ScheduledJobExecutor(executor.getName(), executor.getThreads(), executor.getPeriod(), executor.getMaxRetry()));
                    } else {
                        EXECUTORS.add(new ScheduledJobExecutor(executor.getName(), executor.getThreads(), executor.getPeriod()));
                    }
                }
            }
        }
    }

    public static void stopSchedulers() {
        for (ScheduledJobExecutor executor : EXECUTORS) {
            executor.shutdownExecutor();
        }
    }

    public static ScheduledJobConf.JobConf getScheduledJobConf(String jobName) {
        return JOBS_MAP.get(jobName);
    }
}
