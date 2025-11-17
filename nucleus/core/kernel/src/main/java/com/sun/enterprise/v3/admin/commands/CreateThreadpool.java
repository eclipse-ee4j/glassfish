/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ThreadPools;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;


/**
 * Create Thread Pool Command
 *
 */
@Service(name="create-threadpool")
@PerLookup
@I18n("create.threadpool")
@org.glassfish.api.admin.ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})

public class CreateThreadpool implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new
            LocalStringManagerImpl(CreateThreadpool.class);

    // TODO:  Once Grizzly provides constants for default values, update this class to use those
    // constants: https://grizzly.dev.java.net/issues/show_bug.cgi?id=897 -- jdlee
    @Param(name="maxthreadpoolsize", optional=true, alias="maxThreadPoolSize", defaultValue = "5")
    String maxthreadpoolsize;

    @Param(name="minthreadpoolsize", optional=true, alias="minThreadPoolSize", defaultValue = "2")
    String minthreadpoolsize;

    @Param(name= "idletimeout", optional=true, alias="idleThreadTimeoutSeconds", defaultValue = "900")
    String idletimeout;

    @Param(name="workqueues", optional=true)
    String workqueues;

    @Param(name="maxqueuesize", optional=true, alias="maxQueueSize", defaultValue = "4096")
    String maxQueueSize;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    @Param(name="threadpool_id", primary=true)
    String threadpool_id;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    Domain domain;

    @Inject
    ServiceLocator habitat;

    @AccessRequired.NewChild(type=ThreadPool.class)
    private ThreadPools threadPools;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        config = CLIUtil.updateConfigIfNeeded(config, target, habitat);
        threadPools  = config.getThreadPools();
        for (ThreadPool pool: threadPools.getThreadPool()) {
            final ActionReport report = context.getActionReport();
            if (pool.getName().equals(threadpool_id)) {
                report.setMessage(localStrings.getLocalString("create.threadpool.duplicate",
                        "Thread Pool named {0} already exists.", threadpool_id));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return false;
            }
        }
        return true;
    }


    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        if (workqueues != null) {
            report.setMessage(localStrings.getLocalString("create.threadpool.deprecated.workqueues",
                        "Deprecated Syntax: --workqueues option is deprecated for create-threadpool command."));
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<ThreadPools>() {
                public Object run(ThreadPools param) throws PropertyVetoException, TransactionFailure {
                    ThreadPool newPool = param.createChild(ThreadPool.class);
                    newPool.setName(threadpool_id);
                    newPool.setMaxThreadPoolSize(maxthreadpoolsize);
                    newPool.setMinThreadPoolSize(minthreadpoolsize);
                    newPool.setMaxQueueSize(maxQueueSize);
                    newPool.setIdleThreadTimeoutSeconds(idletimeout);
                    param.getThreadPool().add(newPool);
                    return newPool;
                }
            }, threadPools);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure e) {
            String str = e.getMessage();
            String def = "Creation of: " + threadpool_id + "failed because of: " + str;
            String msg = localStrings.getLocalString("create.threadpool.failed", def, threadpool_id, str);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
