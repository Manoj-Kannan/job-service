package com.omega.jobservice.scheduledjob.frequencyhandler;

import com.omega.jobservice.util.TimeUtil;
import java.util.Calendar;

public class HourlyFrequencyHandler implements FrequencyHandler {
    @Override
    public long nextExecutionTime(long initialExecutionTimeInMillis, long jobTimeInMillis) {
        Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTimeInMillis(initialExecutionTimeInMillis);

        Calendar jobCalendar = Calendar.getInstance();
        jobCalendar.setTimeInMillis(jobTimeInMillis);

        // Set the initial time to the job's time on the same hour as initialExecutionTimeInMillis
        initialCalendar.set(Calendar.MINUTE, jobCalendar.get(Calendar.MINUTE));
        initialCalendar.set(Calendar.SECOND, jobCalendar.get(Calendar.SECOND));
        initialCalendar.set(Calendar.MILLISECOND, 0);

        // Ensure the calculated time is in the future
        while (initialCalendar.getTimeInMillis() <= TimeUtil.currentTimeInMillis()) {
            initialCalendar.add(Calendar.HOUR_OF_DAY, 1);
        }

        return initialCalendar.getTimeInMillis();
    }
}
