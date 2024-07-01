package com.omega.jobservice.scheduledjob;

import com.omega.jobservice.util.TimeUtil;

import java.util.Calendar;

public class ScheduleInfoUtil extends TimeUtil {
    public static void setHourMinSec(Calendar startTimeCalendar, Calendar jobCalendar) {
        startTimeCalendar.set(Calendar.HOUR_OF_DAY, jobCalendar.get(Calendar.HOUR_OF_DAY));
        startTimeCalendar.set(Calendar.MINUTE, jobCalendar.get(Calendar.MINUTE));
        startTimeCalendar.set(Calendar.SECOND, jobCalendar.get(Calendar.SECOND));
        startTimeCalendar.set(Calendar.MILLISECOND, 0);
    }
}
