/*
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

import java.beans.PropertyVetoException;
import java.util.List;

import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.api.admin.*;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;

import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.grizzly.config.dom.NetworkListener;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport.ExitCode;

@Service(name="delete-threadpool")
@PerLookup
@I18n("delete.threadpool")
@org.glassfish.api.admin.ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
public class DeleteThreadpool implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteThreadpool.class);

    @Param(name="threadpool_id", primary=true)
    String threadpool_id;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    Configs configs;

    @Inject
    Domain domain;

    @Inject
    ServiceLocator habitat;

    private ThreadPools threadPools;

    @AccessRequired.To("delete")
    private ThreadPool pool;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        config = CLIUtil.updateConfigIfNeeded(config, target, habitat);
        threadPools  = config.getThreadPools();
        if(!isThreadPoolExists(threadPools)) {
            report.setMessage(localStrings.getLocalString("delete.threadpool.notexists",
                "Thread Pool named {0} does not exist.", threadpool_id));
            report.setActionExitCode(ExitCode.FAILURE);
            return false;
        }
        pool = null;
        for (ThreadPool tp : config.getThreadPools().getThreadPool()) {
            if (tp.getName().equals(threadpool_id)) {
                pool = tp;
            }
        }

        List<NetworkListener> nwlsnrList = pool.findNetworkListeners();
        for (NetworkListener nwlsnr : nwlsnrList) {
            if (pool.getName().equals(nwlsnr.getThreadPool())) {
                report.setMessage(localStrings.getLocalString(
                    "delete.threadpool.beingused",
                    "{0} threadpool is being used in the network listener {1}",
                    threadpool_id, nwlsnr.getName()));
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
        ActionReport report = context.getActionReport();



        try {
            ConfigSupport.apply(new SingleConfigCode<ThreadPools>() {
                public Object run(ThreadPools param) throws PropertyVetoException,
                        TransactionFailure {
                    List<ThreadPool> poolList = param.getThreadPool();
                    for (ThreadPool pool : poolList) {
                        String currPoolId = pool.getName();
                        if (currPoolId != null && currPoolId.equals
                                (threadpool_id)) {
                            poolList.remove(pool);
                            break;
                        }
                    }
                    return poolList;
                }
            }, threadPools);
            report.setActionExitCode(ExitCode.SUCCESS);
        } catch(TransactionFailure e) {
            String str = e.getMessage();
            report.setMessage(localStrings.getLocalString("delete.threadpool" +
                    ".failed", "Delete Thread Pool failed because of: ", str));
            report.setActionExitCode(ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private boolean isThreadPoolExists(ThreadPools threadPools) {

        for (ThreadPool pool : threadPools.getThreadPool()) {
            String currPoolId = pool.getName();
            if (currPoolId != null && currPoolId.equals(threadpool_id)) {
                return true;
            }
        }
        return false;
    }

}
