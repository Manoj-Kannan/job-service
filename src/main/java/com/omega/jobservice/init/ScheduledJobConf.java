package com.omega.jobservice.init;

import com.omega.jobservice.scheduledjob.ScheduledJob;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@Setter
@XmlRootElement(name = "scheduler")
public class ScheduledJobConf {
    private static final Logger LOGGER = LogManager.getLogger(ScheduledJobConf.class.getName());

    private List<JobConf> jobs;

    @XmlElement(name = "job")
    public List<JobConf> getJobs() {
        return jobs;
    }

    @Setter
    public final static class JobConf {
        private String name;
        private String className;
        private int transactionTimeout;
        private Class<? extends ScheduledJob> classObject = null;

        public JobConf() {
        }

        @XmlAttribute(name = "name")
        public String getName() {
            return name;
        }

        @XmlAttribute(name = "classname")
        public String getClassName() {
            return className;
        }

        @XmlAttribute(name = "transactionTimeout")
        public int getTransactionTimeout() {
            return transactionTimeout;
        }

        public Class<? extends ScheduledJob> getClassObject() {
            if (classObject != null) {
                return classObject;
            }
            try {
                if (className != null && !className.isEmpty()) {
                    classObject = (Class<? extends ScheduledJob>) Class.forName(className);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.FATAL, "Exception occurred while initiating job : " + name, e);
            }
            return classObject;
        }

        @Override
        public String toString() {
            return "ScheduledJobConf{name, className, transactionTimeout}={" + name + "," + className + "," + transactionTimeout + "}";
        }
    }
}
