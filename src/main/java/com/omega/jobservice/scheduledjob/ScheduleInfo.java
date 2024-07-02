package com.omega.jobservice.scheduledjob;

import com.omega.jobservice.scheduledjob.frequencyhandler.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
public class ScheduleInfo implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(ScheduleInfo.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Long jobTimeInMillis;
    private Integer frequencyType = FrequencyType.DO_NOT_REPEAT.getIndex();

    @Transient
    private FrequencyType frequencyTypeEnum = FrequencyType.DO_NOT_REPEAT;

    public FrequencyType getFrequencyTypeEnum() {
        if (frequencyTypeEnum == null) {
            frequencyTypeEnum = FrequencyType.FREQUENCY_TYPE_MAP.get(frequencyType);
        }
        return frequencyTypeEnum;
    }

    public long nextExecutionTime(long initialExecTime) {
        FrequencyHandler frequencyTypeHandler = getFrequencyTypeEnum().getFrequencyTypeHandler();
        long nextExecutionTime = frequencyTypeHandler.nextExecutionTime(initialExecTime, jobTimeInMillis);

        return nextExecutionTime;
    }

    @Getter
    public enum FrequencyType {
        HOURLY(1, "Hourly", new HourlyFrequencyHandler()),
        DAILY(2, "Daily", new DailyFrequencyHandler()),
        WEEKLY(3, "Weekly", new WeeklyFrequencyHandler()),
        MONTHLY(4, "Monthly", new MonthlyFrequencyHandler()),
        QUARTERLY(5, "Quarterly", new QuarterlyFrequencyHandler()),
        HALFYEARLY(6, "Half-Yearly", new HalfyearlyFrequencyHandler()),
        YEARLY(7, "Yearly", new YearlyFrequencyHandler()),
        DO_NOT_REPEAT(8, "Do Not Repeat", new DoNotRepeatFrequencyHandler());

        FrequencyType(int index, String description, FrequencyHandler frequencyTypeHandler) {
            this.index = index;
            this.description = description;
            this.frequencyTypeHandler = frequencyTypeHandler;
        }

        private final int index;
        private final String description;
        private final FrequencyHandler frequencyTypeHandler;
        private static final Map<Integer, FrequencyType> FREQUENCY_TYPE_MAP = Collections.unmodifiableMap(initTypeMap());

        private static Map<Integer, FrequencyType> initTypeMap() {
            Map<Integer, FrequencyType> typeMap = new HashMap<>();
            for(FrequencyType type : values()) {
                typeMap.put(type.getIndex(), type);
            }
            return typeMap;
        }
    }
}
