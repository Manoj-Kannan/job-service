package com.omega.jobservice.commands;

import com.omega.jobservice.scheduledjob.ScheduledJob;
import com.omega.jobservice.jobconfig.JobContext;
import com.omega.jobservice.util.JobConstants;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

public class ScheduledJobExecutionCommand implements Command {
    @Override
    public boolean execute(Context context) throws Exception {
        ScheduledJob scheduledJob = (ScheduledJob) context.get(JobConstants.SCHEDULED_JOB);
        JobContext jobContext = (JobContext) context.get(JobConstants.JOB_CONTEXT);
        scheduledJob.execute(jobContext);

        return false;
    }
}
