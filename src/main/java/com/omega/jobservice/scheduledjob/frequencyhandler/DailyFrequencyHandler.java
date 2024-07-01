package com.omega.jobservice.scheduledjob.frequencyhandler;

import com.omega.jobservice.scheduledjob.ScheduleInfoUtil;
import com.omega.jobservice.util.TimeUtil;

import java.util.Calendar;

public class DailyFrequencyHandler implements FrequencyHandler {
    @Override
    public long nextExecutionTime(long initialExecutionTimeInMillis, long jobTimeInMillis) {
        Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTimeInMillis(initialExecutionTimeInMillis);

        Calendar jobCalendar = Calendar.getInstance();
        jobCalendar.setTimeInMillis(jobTimeInMillis);

        // Set the time to the desired job time on the same day as initialExecutionTimeInMillis
        ScheduleInfoUtil.setHourMinSec(initialCalendar, jobCalendar);

        // Ensure the calculated time is in the future
        while (initialCalendar.getTimeInMillis() <= TimeUtil.currentTimeInMillis()) {
            initialCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return initialCalendar.getTimeInMillis();
    }
}
