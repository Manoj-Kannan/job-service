package com.omega.jobservice.scheduledjob;

import com.omega.jobservice.init.ScheduledJobConf;
import com.omega.jobservice.jobconfig.JobConfig;
import com.omega.jobservice.init.ScheduledJobExecutorConf;
import org.apache.commons.lang3.StringUtils;
import com.omega.jobservice.BeanFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

@Component
public class ScheduledJobController {
    private static final Logger LOGGER = LogManager.getLogger(ScheduledJobController.class.getName());

    private static final Map<String, ScheduledJobConf.JobConf> JOBS_MAP = new HashMap<>();
    private static final List<ScheduledJobExecutor> EXECUTORS = new ArrayList<>();

    @Getter
    @Autowired
    private static JobConfig config = null;

    @Autowired
    private ApplicationContext applicationContext;

    public synchronized void initScheduler(JobConfig jobConfig) throws JAXBException {
        config = jobConfig;

        parseJobConfObject();
        if (jobConfig.isEnabledService()) {
            parseExecutorConfObject();
        }

        LOGGER.info("Scheduler Jobs : " + JOBS_MAP);
        LOGGER.info("Scheduler Executors : " + EXECUTORS);
    }

    private void parseJobConfObject() throws JAXBException {
        ClassLoader classLoader = ScheduledJobController.class.getClassLoader();
        File schedulerXml = new File(classLoader.getResource(config.getJobFilePath()).getFile());

        JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[]{ScheduledJobConf.class}, null);
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

    private void parseExecutorConfObject() throws JAXBException {
        ClassLoader classLoader = ScheduledJobController.class.getClassLoader();
        File executorsXml = new File(classLoader.getResource(config.getExecFilePath()).getFile());

        JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[]{ScheduledJobExecutorConf.class}, null);
        ScheduledJobExecutorConf executorsConf = (ScheduledJobExecutorConf) jaxbContext.createUnmarshaller().unmarshal(executorsXml);

        if (executorsConf.getExecutors() != null) {
            for (ScheduledJobExecutorConf.Executor executor : executorsConf.getExecutors()) {
                if (StringUtils.isNotEmpty(executor.getName()) && executor.getPeriod() != -1 && executor.getThreads() != -1) {
                    int maxRetry = executor.getMaxRetry() > 0 ? executor.getMaxRetry() : 0;
                    BeanFactory beanFactory = applicationContext.getBean(BeanFactory.class);
                    ScheduledJobExecutor scheduledJobExecutor = beanFactory.createScheduledExecutor(executor.getName(), executor.getThreads(), executor.getPeriod(), maxRetry);

                    EXECUTORS.add(scheduledJobExecutor);
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
