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

package org.glassfish.flashlight.cli;

import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.beans.PropertyVetoException;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import com.sun.enterprise.config.serverbeans.MonitoringService;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.jvnet.hk2.config.Dom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sreenivas Munnangi
 */
@Service(name="monitoring-config")
public class MonitoringConfig {

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(MonitoringConfig.class);

    private static AtomicBoolean valueUpdated = new AtomicBoolean(false);

    static void setMonitoringEnabled(MonitoringService ms,
        final String enabled, final ActionReport report) {

        try {
            ConfigSupport.apply(new SingleConfigCode<MonitoringService>() {
                public Object run(MonitoringService param)
                throws PropertyVetoException, TransactionFailure {
                    param.setMonitoringEnabled(enabled);
                    return param;
                }
            }, ms);
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("disable.monitoring.exception",
                "Encountered exception while setting monitoring-enabled to false {0}", tfe.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }


    static void setMBeanEnabled(MonitoringService ms,
        final String enabled, final ActionReport report) {

        try {
            ConfigSupport.apply(new SingleConfigCode<MonitoringService>() {
                public Object run(MonitoringService param)
                throws PropertyVetoException, TransactionFailure {
                    param.setMbeanEnabled(enabled);
                    return param;
                }
            }, ms);
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("disable.monitoring.exception",
                "Encountered exception while setting mbean-enabled to false {0}", tfe.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }


    static void setDTraceEnabled(MonitoringService ms,
        final String enabled, final ActionReport report) {

        try {
            ConfigSupport.apply(new SingleConfigCode<MonitoringService>() {
                public Object run(MonitoringService param)
                throws PropertyVetoException, TransactionFailure {
                    param.setDtraceEnabled(enabled);
                    return param;
                }
            }, ms);
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("disable.monitoring.exception",
                "Encountered exception while setting dtrace-enabled to false {0}", tfe.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }

    static void setMonitoringLevel(MonitoringService ms,
        final String moduleName, final String level, final ActionReport report) {

        if (ms.getMonitoringLevel(moduleName) == null) {
            report.setMessage(localStrings.getLocalString("invalid.module",
                    "Invalid module name {0}",
                    moduleName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<MonitoringService>() {

                public Object run(MonitoringService param)
                        throws PropertyVetoException, TransactionFailure {
                    param.setMonitoringLevel(moduleName, level);
                    return null;
                }
            }, ms);
        } catch (TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("monitoring.config.exception",
                    "Encountered exception {0} while setting monitoring level to {1} for {2}",
                    tfe.getMessage(), level, moduleName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }

    static void setMonitoringLevelX(MonitoringService ms,
        final String moduleName, final String level, final ActionReport report) {

        ModuleMonitoringLevels mmls = ms.getModuleMonitoringLevels();
        //TODO: synchronize
        try {
            ConfigSupport.apply(new SingleConfigCode<ModuleMonitoringLevels>() {
                public Object run(ModuleMonitoringLevels param)
                throws PropertyVetoException, TransactionFailure {
                    Dom dom = Dom.unwrap(param);
                    String currentVal = dom.attribute(moduleName);
                    if (currentVal == null) {
                        valueUpdated.set(false);
                        return null;
                    } else {
                        dom.attribute(moduleName, level);
                    }
                    return param;
                }
            }, mmls);
        } catch(TransactionFailure tfe) {
            valueUpdated.set(false);
            report.setMessage(localStrings.getLocalString("disable.monitoring.level",
                "Encountered exception {0} while setting monitoring level to OFF for {1}",
                tfe.getMessage(), moduleName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        if (!valueUpdated.get()) {
            setContainerMonitoringLevel(ms, moduleName, level, report);
        }
    }


    static boolean setContainerMonitoringLevel(MonitoringService ms,
        final String moduleName, final String level, final ActionReport report) {

        ContainerMonitoring cm = ms.getContainerMonitoring(moduleName);
        if (cm == null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage(localStrings.getLocalString("invalid.module",
                "Invalid module name {0}", moduleName));
            return false;
        }
        try {
            ConfigSupport.apply(new SingleConfigCode<ContainerMonitoring>() {
                public Object run(ContainerMonitoring param)
                throws PropertyVetoException, TransactionFailure {
                    param.setLevel(level);
                    return param;
                }
            }, cm);
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("disable.monitoring.level",
                "Encountered exception {0} while setting monitoring level to OFF for {1}",
                tfe.getMessage(), moduleName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        return true;
    }
}
