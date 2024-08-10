/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ManagedJobConfig;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;



/**
 * This command manages configured jobs
 * Managed jobs are commands which are annotated with @ManagedJob ,@Progress
 * or running with --detach
 * You can configure the job retention period, job inactivity period,initial-delay,poll-interval
 * persisting options for those jobs which will be used by the Job Manager
 * to purge the jobs according to the criteria specified.
 * Definition of parameters:
 * job-retention-period - Time period to store the jobs. Defaults 24 hours.
 *
 * job-inactivity-period  -Time period after which we expire an inactive, non responsive command
 *
 * initial-delay - Initial delay after which the cleanup service should start purging
 * This is useful when the server restarts will provide some time for the Job Manager to
 * bootstrap
 *
 * poll-interval - The time interval after which the JobCleanupService should poll for expired jobs
 *

 *
 * @author Bhakti Mehta
 */
@Service(name = "configure-managed-jobs")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@AccessRequired(resource="domain/managed-job-config", action="update")
public class ConfigureManagedJobs implements AdminCommand {

    @Inject
    Domain domain;

    @Param(name="in-memory-retention-period", optional=true)
    String inMemoryRetentionPeriod;

    @Param(name="job-retention-period", optional=true)
    String jobRetentionPeriod;

    @Param(name="cleanup-initial-delay", optional=true)
    String initialDelay;

    @Param(name="cleanup-poll-interval", optional=true)
    String pollInterval;



    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Logger logger= context.getLogger();

        ManagedJobConfig managedJobConfig = domain.getExtensionByType(ManagedJobConfig.class);
        if (managedJobConfig == null ) {
           logger.warning(KernelLoggerInfo.getFailManagedJobConfig);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(KernelLoggerInfo.getLogger().getResourceBundle().getString(KernelLoggerInfo.getFailManagedJobConfig));
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<ManagedJobConfig>() {

                @Override
                public Object run(ManagedJobConfig param) throws PropertyVetoException, TransactionFailure {

                    if (inMemoryRetentionPeriod != null)
                        param.setInMemoryRetentionPeriod(inMemoryRetentionPeriod);
                    if (jobRetentionPeriod != null)
                        param.setJobRetentionPeriod(jobRetentionPeriod);
                    if (pollInterval != null)
                        param.setPollInterval(pollInterval);
                    if (initialDelay != null)
                        param.setInitialDelay(initialDelay);

                    return param;
                }
            }, managedJobConfig);

        } catch(TransactionFailure e) {
            logger.warning(KernelLoggerInfo.configFailManagedJobConfig);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
        }

    }

}



