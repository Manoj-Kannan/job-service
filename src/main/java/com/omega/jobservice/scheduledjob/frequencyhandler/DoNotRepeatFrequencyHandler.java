package com.omega.jobservice.scheduledjob.frequencyhandler;

import com.omega.jobservice.util.TimeUtil;

public class DoNotRepeatFrequencyHandler implements FrequencyHandler {
    @Override
    public long nextExecutionTime(long initialExecutionTimeInMillis, long jobTimeInMillis) {
        if (initialExecutionTimeInMillis > TimeUtil.currentTimeInMillis()) {
            return initialExecutionTimeInMillis;
        } else {
            // If it is in the past, return the current time plus a small offset (1 second)
            return TimeUtil.currentTimeInMillis() + TimeUtil.ONE_SECOND;
        }
    }
}
