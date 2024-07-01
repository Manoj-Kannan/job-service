package com.omega.jobservice.scheduledjob.frequencyhandler;

import com.omega.jobservice.scheduledjob.ScheduleInfoUtil;
import com.omega.jobservice.util.TimeUtil;

import java.util.Calendar;

public class HalfyearlyFrequencyHandler implements FrequencyHandler {
    @Override
    public long nextExecutionTime(long initialExecutionTimeInMillis, long jobTimeInMillis) {
        Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTimeInMillis(initialExecutionTimeInMillis);

        Calendar jobCalendar = Calendar.getInstance();
        jobCalendar.setTimeInMillis(jobTimeInMillis);

        // Set calendar to the job's time, month, and day of the month
        initialCalendar.set(Calendar.MONTH, initialCalendar.get(Calendar.MONTH) + 6);
        initialCalendar.set(Calendar.DAY_OF_MONTH, jobCalendar.get(Calendar.DAY_OF_MONTH));
        ScheduleInfoUtil.setHourMinSec(initialCalendar, jobCalendar);

        // Ensure the calculated time is in the future
        while (initialCalendar.getTimeInMillis() <= TimeUtil.currentTimeInMillis()) {
            initialCalendar.add(Calendar.MONTH, 6);
        }

        return initialCalendar.getTimeInMillis();
    }
}
