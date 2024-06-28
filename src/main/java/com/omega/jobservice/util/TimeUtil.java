package com.omega.jobservice.util;

import java.time.Instant;

public class TimeUtil {
    public static final long ONE_SECOND = 1000;
    public static final long ONE_MINUTE_IN_SECONDS = 60;
    public static final long ONE_MINUTE_IN_MILLIS = 60000;
    public static final long ONE_DAY_IN_SECONDS = 86400;
    public static final long ONE_DAY_IN_MILLIS = 86400000;

    public static long currentTimeInMillis() {
        return System.currentTimeMillis();
    }

    public static long currentTimeInSeconds() {
        return Instant.now().getEpochSecond();
    }

    public static long timeInSeconds(long milliSeconds) {
        return milliSeconds / ONE_SECOND;
    }

    public static long timeInMilliSeconds(long seconds) {
        return seconds * ONE_SECOND;
    }

    public static long calculateSecondsDifference(long fromSeconds) {
        long toSeconds = currentTimeInSeconds();
        return toSeconds - fromSeconds;
    }

    public static long calculateMilliSecDifference(long fromMilliSeconds) {
        long toMilliSeconds = currentTimeInMillis();
        return toMilliSeconds - fromMilliSeconds;
    }
}
