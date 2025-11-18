/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.monitor;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * A POL (plain old logger).
 *
 * @author bnevins
 */
public class MLogger {

    public static Logger getLogger() {
        return logger;
    }

    private MLogger() {
    }

    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "com.sun.enterprise.admin.monitor.LogMessages";
    @LoggerInfo(subsystem = "Monitoring", description = "Monitoring Logger", publish = true)
    public static final String LOGGER_NAME = "jakarta.enterprise.monitoring";
    private final static Logger logger = Logger.getLogger(LOGGER_NAME, SHARED_LOGMESSAGE_RESOURCE);
    @LogMessageInfo(message = "Flashlight listener registration failed for listener class: {0} , will retry later", comment = "see message", cause = "see message", action = "see message", level = "WARNING")
    public static final String ListenerRegistrationFailed = "NCLS-MNTG-00201";
    @LogMessageInfo(message = "Unable to create container-monitoring for {0}.", comment = "see message", cause = "see message", action = "see message", level = "SEVERE")
    public static final String cannotCreateConfigElement = "NCLS-MNTG-00210";
    @LogMessageInfo(message = "Invalid statsProvider (very likely a duplicate request), cannot unregister: {0}", comment = "see message", cause = "see message", action = "see message", level = "INFO")
    public static final String invalidStatsProvider = "NCLS-MNTG-00202";
    @LogMessageInfo(message = "Error unregistering the stats provider  {0}", comment = "see message", cause = "see message", action = "see message", level = "SEVERE")
    public static final String errorUnregisteringStatsProvider = "NCLS-MNTG-00208";
    @LogMessageInfo(message = "Cannot find node {0} for statsProvider {1}", comment = "see message", cause = "see message", action = "see message", level = "WARNING")
    public static final String nodeNotFound = "NCLS-MNTG-00203";
    @LogMessageInfo(message = "Error resetting the stats provider: {0}", comment = "see message", cause = "see message", action = "see message", level = "SEVERE")
    public static final String errorResettingStatsProvider = "NCLS-MNTG-00209";
    @LogMessageInfo(message = "{0} is not a ManagedObject and will not be registered with Gmbal to create an MBean", comment = "see message", cause = "see message", action = "see message", level = "INFO")
    public static final String notaManagedObject = "NCLS-MNTG-00204";
    @LogMessageInfo(message = "Gmbal registration failed", comment = "see message", cause = "see message", action = "see message", level = "SEVERE")
    public static final String gmbalRegistrationFailed = "NCLS-MNTG-00205";
    @LogMessageInfo(message = "Gmbal unregistration failed", comment = "see message", cause = "see message", action = "see message", level = "SEVERE")
    public static final String gmbalUnRegistrationFailed = "NCLS-MNTG-00206";
    @LogMessageInfo(message = "module-monitoring-level or container-monitoring config element for {0} does not exist", comment = "see message", cause = "see message", action = "see message", level = "WARNING")
    public static final String monitorElementDoesnotExist = "NCLS-MNTG-00207";
    @LogMessageInfo(message = "Unable to load the ProbeProvider", comment = "see message", cause = "see message", action = "see message", level = "SEVERE")
    public static final String unableToLoadProbeProvider = "NCLS-MNTG-00104";
    @LogMessageInfo(message = "Unable to load the ProbeProvider", comment = "see message", cause = "see message", action = "see message", level = "SEVERE")
    public static final String unableToProcessXMLProbeProvider = "NCLS-MNTG-00105";
    @LogMessageInfo(message = "Missing Module ({0})  From Xml Probe Providers", comment = "see message", cause = "see message", action = "see message", level = "SEVERE")
    public static final String monitoringMissingModuleFromXmlProbeProviders = "NCLS-MNTG-00005";
    @LogMessageInfo(message = "mbean-enabled flag is turned on. Enabling all the MBeans", comment = "see message", cause = "see message", action = "see message", level = "INFO")
    public static final String mbeanEnabled = "NCLS-MNTG-00109";
    @LogMessageInfo(message = "mbean-enabled flag is turned off. Disabling all the MBeans", comment = "see message", cause = "see message", action = "see message", level = "INFO")
    public static final String mbeanDisabled = "NCLS-MNTG-00110";
    @LogMessageInfo(message = "dtrace-enabled flag is turned on/off. Enabling/Disabling DTrace", comment = "see message", cause = "see message", action = "see message", level = "INFO")
    public static final String dtraceEnabled = "NCLS-MNTG-00111";
    @LogMessageInfo(message = "monitoring-enabled flag is turned on. Enabling all the Probes and Stats", comment = "see message", cause = "see message", action = "see message", level = "INFO")
    public static final String monitoringEnabledLogMsg = "NCLS-MNTG-00112";
    @LogMessageInfo(message = "monitoring-enabled flag is turned off. Disabling all the Stats", comment = "see message", cause = "see message", action = "see message", level = "INFO")
    public static final String monitoringDisabledLogMsg = "NCLS-MNTG-00113";
}
