package com.omega.jobservice.commands;

import com.omega.jobservice.instantjob.InstantJob;
import com.omega.jobservice.util.JobConstants;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

public class InstantJobExecutionCommand implements Command {
    @Override
    public boolean execute(Context context) throws Exception {
        InstantJob instantJob = (InstantJob) context.get(JobConstants.SCHEDULED_JOB);
        instantJob.execute(context);

        return false;
    }
}
