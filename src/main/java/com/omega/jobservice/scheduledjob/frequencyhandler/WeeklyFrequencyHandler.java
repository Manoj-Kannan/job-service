package com.omega.jobservice.scheduledjob.frequencyhandler;

import com.omega.jobservice.scheduledjob.ScheduleInfoUtil;
import com.omega.jobservice.util.TimeUtil;

import java.util.Calendar;

public class WeeklyFrequencyHandler implements FrequencyHandler {

    @Override
    public long nextExecutionTime(long initialExecutionTimeInMillis, long jobTimeInMillis) {
        Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTimeInMillis(initialExecutionTimeInMillis);

        Calendar jobCalendar = Calendar.getInstance();
        jobCalendar.setTimeInMillis(jobTimeInMillis);

        // Set calendar to the job's time and day of the week
        initialCalendar.set(Calendar.DAY_OF_WEEK, jobCalendar.get(Calendar.DAY_OF_WEEK));
        ScheduleInfoUtil.setHourMinSec(initialCalendar, jobCalendar);

        // Ensure the calculated time is in the future
        while (initialCalendar.getTimeInMillis() <= TimeUtil.currentTimeInMillis()) {
            initialCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return initialCalendar.getTimeInMillis();
    }
}
