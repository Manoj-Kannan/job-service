package com.omega.jobservice.init;

import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Set;

@Setter
@XmlRootElement(name = "executors")
public class ScheduledJobExecutorConf {

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

        @XmlAttribute(name = "name")
        public String getName() {
            return name;
        }

        private int period = -1;

        @XmlAttribute(name = "period")
        public int getPeriod() {
            return period;
        }

        private int threads = -1;

        @XmlAttribute(name = "threads")
        public int getThreads() {
            return threads;
        }

        private int maxRetry = -1;

        @XmlAttribute(name = "maxRetry")
        public int getMaxRetry() {
            return maxRetry;
        }

        @Override
        public String toString() {
            return new StringBuilder("ExecutorConf{")
                    .append("name=").append(name)
                    .append(", period=").append(period)
                    .append(", threads=").append(threads)
                    .append(", maxRetry=").append(maxRetry)
                    .append('}')
                    .toString();
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}
