package com.omega.jobservice.init;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;

import com.omega.jobservice.instantjob.InstantJob;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import lombok.Setter;

import java.util.List;

@XmlRootElement(name = "instantjob")
public class InstantJobConf {
    private static final Logger LOGGER = LogManager.getLogger(InstantJobConf.class.getName());
    public static final int DEFAULT_TIME_OUT = 300; //In Seconds
    public static final int JOB_TIMEOUT_BUFFER = 5;

    private List<InstantJobConf.JobConf> jobs;

    @XmlElement(name = "job")
    public List<InstantJobConf.JobConf> getJobs() {
        return jobs;
    }

    @Setter
    public final static class JobConf {
        private String name, className;
        private int transactionTimeout;
        private Class<? extends InstantJob> classObject = null;

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

        public Class<? extends InstantJob> getClassObject() {
            if (classObject != null) {
                return classObject;
            }
            try {
                if (className != null && !className.isEmpty()) {
                    classObject = (Class<? extends InstantJob>) Class.forName(className);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.FATAL, "Exception occurred while initiating job : " + name, e);
            }
            return classObject;
        }

        @Override
        public String toString() {
            return "InstantJobConf{name, className, transactionTimeout}={" + name + "," + className + "," + transactionTimeout + "}";
        }
    }
}
