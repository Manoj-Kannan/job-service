package com.omega.jobservice.init;

import lombok.Setter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@Setter
@XmlRootElement(name = "executors")
public class InstantJobExecutorConf {

    private Set<Executor> executors;

    @XmlElement(name = "executor")
    public Set<Executor> getExecutors() {
        return executors;
    }

    @Setter
    public final static class Executor {
        public Executor() {
        }

        private String name;
        private int queueSize = -1;
        private int maxThreads = -1;
        private int dataRetention = -1;
        private int pollingFrequency = -1;

        @XmlAttribute(name = "name")
        public String getName() {
            return name;
        }

        @XmlAttribute(name = "queueSize")
        public int getQueueSize() {
            return queueSize;
        }

        @XmlAttribute(name = "maxThreads")
        public int getMaxThreads() {
            return maxThreads;
        }

        @XmlAttribute(name = "dataRetention")
        public int getDataRetention() {
            return dataRetention;
        }

        @XmlAttribute(name = "pollingFrequency")
        public int getPollingFrequency() {
            return pollingFrequency;
        }

        @Override
        public String toString() {
            return new StringBuilder("ExecutorConf{")
                    .append("name=").append(name)
                    .append(", queueSize=").append(queueSize)
                    .append(", maxThreads=").append(maxThreads)
                    .append(", dataRetention=").append(dataRetention)
                    .append(", pollingFrequency=").append(pollingFrequency)
                    .append('}')
                    .toString();
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}
