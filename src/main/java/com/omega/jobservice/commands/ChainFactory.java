package com.omega.jobservice.commands;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;

public class ChainFactory extends ChainBase {

    public static ChainBase scheduledJobExecutionChain(long transactionTimeOut) {
        ChainBase chain = new ChainBase();
        chain.addCommand(new ScheduledJobExecutionCommand());
        return chain;
    }

    public static ChainBase instantJobExecutionChain(long transactionTimeOut) {
        ChainBase chain = new ChainBase();
        chain.addCommand(new InstantJobExecutionCommand());
        return chain;
    }
}
