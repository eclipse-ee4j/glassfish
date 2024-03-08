/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.loadbalancer.admin.cli;

import com.sun.enterprise.config.serverbeans.*;
import java.util.logging.Logger;
import java.beans.PropertyVetoException;


import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.*;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.util.LocalStringManagerImpl;

import org.glassfish.api.admin.*;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.hk2.api.PerLookup;

import jakarta.inject.Inject;

/**
 * This is a remote command that disables lb-enabled attribute of an application
 * for cluster or instance
 * @author Yamini K B
 */
@Service(name = "disable-http-lb-application")
@PerLookup
@TargetType(value={CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@org.glassfish.api.admin.ExecuteOn(RuntimeType.DAS)
@RestEndpoints({
    @RestEndpoint(configBean=Application.class,
        opType=RestEndpoint.OpType.POST,
        path="disable-http-lb-application",
        description="disable-http-lb-application",
        params={
            @RestParam(name="name", value="$parent")
        })
})
public final class DisableHTTPLBApplicationCommand implements AdminCommand {

    @Param(primary=true)
    String target;

    @Param(optional=false)
    String name;

    @Param(optional=true, defaultValue="30")
    String timeout;

    @Inject
    Domain domain;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(DisableHTTPLBApplicationCommand.class);

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        Logger logger = context.getLogger();

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        ApplicationRef appRef = domain.getApplicationRefInTarget(name, target);

        if (appRef == null) {
            String msg = localStrings.getLocalString("AppRefNotDefined",
                    "Application ref [{0}] does not exist in server [{1}]", name, target);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        boolean appEnabled = Boolean.valueOf(appRef.getEnabled());

        if (appEnabled) {
            if (appRef.getLbEnabled().equals("false")) {
                String msg = localStrings.getLocalString("AppDisabled",
                        "Application [{0}] is already disabled for [{1}].", name, target);
                logger.warning(msg);
                report.setMessage(msg);
            } else {
                try {
                    updateLbEnabledForApp(name, target, timeout);
                } catch(TransactionFailure e) {
                    String msg = localStrings.getLocalString("FailedToUpdateAttr",
                            "Failed to update lb-enabled attribute for {0}", name);
                    logger.warning(msg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(msg);
                    report.setFailureCause(e);
                }
            }
        }
    }

    public void updateLbEnabledForApp(final String appName,
        final String target, final String timeout) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode() {
            @Override
            public Object run(ConfigBeanProxy param) throws PropertyVetoException, TransactionFailure {
                // get the transaction
                Transaction t = Transaction.getTransaction(param);
                if (t!=null) {
                    Server servr = ((Domain)param).getServerNamed(target);
                    if (servr != null) {
                        // update the application-ref from standalone
                        // server instance
                        for (ApplicationRef appRef :
                            servr.getApplicationRef()) {
                            if (appRef.getRef().equals(appName)) {
                                ConfigBeanProxy appRef_w = t.enroll(appRef);
                                ((ApplicationRef)appRef_w).setLbEnabled("false");
                                ((ApplicationRef)appRef_w).setDisableTimeoutInMinutes(timeout);
                                break;
                            }
                        }
                    }
                    Cluster cluster = ((Domain)param).getClusterNamed(target);
                    if (cluster != null) {
                        // update the application-ref from cluster
                        for (ApplicationRef appRef :
                            cluster.getApplicationRef()) {
                            if (appRef.getRef().equals(appName)) {
                                ConfigBeanProxy appRef_w = t.enroll(appRef);
                                ((ApplicationRef)appRef_w).setLbEnabled("false");
                                ((ApplicationRef)appRef_w).setDisableTimeoutInMinutes(timeout);
                                break;
                            }
                        }

                        // update the application-ref from cluster instances
                        for (Server svr : cluster.getInstances() ) {
                            for (ApplicationRef appRef :
                                svr.getApplicationRef()) {
                                if (appRef.getRef().equals(appName)) {
                                    ConfigBeanProxy appRef_w = t.enroll(appRef);
                                    ((ApplicationRef)appRef_w).setLbEnabled("false");
                                    ((ApplicationRef)appRef_w).setDisableTimeoutInMinutes(timeout);
                                    break;
                                }
                            }
                        }
                    }
             }
             return Boolean.TRUE;
            }
        }, domain);
    }

}
