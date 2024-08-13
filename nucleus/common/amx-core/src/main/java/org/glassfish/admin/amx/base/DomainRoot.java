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

package org.glassfish.admin.amx.base;

import java.util.List;
import java.util.Map;

import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.PathnameConstants;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.logging.Logging;
import org.glassfish.admin.amx.monitoring.MonitoringRoot;
import org.glassfish.external.amx.AMX;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;


/**
The top-level interface for an appserver domain. Access to all other
{@link AMXProxy} begins here.
<p>
Not all children of DomainRoot have getter method; they could be added
dynamically.
<p>
The 'name' property in the ObjectName of DomainRoot is the name of the
appserver domain.  For example, appserver domains 'domain' and 'domain2' would
have ObjectNames for DomainRoot as follows:
<pre>
amx:type=DomainRoot:name=domain1
amx:type=DomainRoot:name=domain2
</pre>
Of course, these two MBeans would normally be found in different MBeanServers.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@AMXMBeanMetadata(singleton = true, globalSingleton = true)
public interface DomainRoot extends AMXProxy
{
    public static final String PARENT_PATH = "";

    public static final String PATH = PARENT_PATH + PathnameConstants.SEPARATOR;

    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    @Description("Stop the domain immediately")
    public void stopDomain();

    /**
    Return the {@link Ext} MBean, parent of top-level utility and specialty MBeans.
     */
    @ManagedAttribute
    @Description("Get the primary extension point for AMX MBeans other than monitoring")
    public Ext getExt();

    /**
    Return the {@link Tools} MBean.
     */
    @ManagedAttribute
    @Description("Get the Tools MBean")
    public Tools getTools();

    /**
    @return the singleton {@link Query}.
     */
    @ManagedAttribute
    @Description("Get the Query MBean")
    public Query getQueryMgr();

    /**
    @return the singleton {@link Logging}.
     */
    @ManagedAttribute
    @Description("Get the Logging MBean")
    public Logging getLogging();

    /**
    @return the singleton {@link BulkAccess}.
     */
    @ManagedAttribute
    @Description("Get the BulkAccess MBean")
    public BulkAccess getBulkAccess();

    /**
    @return the singleton {@link Pathnames}.
     */
    @ManagedAttribute
    public Pathnames getPathnames();

    /**
    @return the singleton {@link Sample}.
     */
    @ManagedAttribute
    public Sample getSample();

    /**
    Return the name of this appserver domain.  Not to be confused with the
    JMX domain name, which may be derived from this name and is
    available from any ObjectName in AMX by calling
    {@link Util#getObjectName}

    The domain name is equivalent to the name of
    the directory containing the domain configuration.  This name
    is not part of the configuration and can only be changed by
    using a different directory to house the configuration for the
    domain.
    @return the name of the Appserver domain
     */
    @ManagedAttribute
    public String getAppserverDomainName();

    /**
    For module dependency reasons, the returned object must be cast to the appropriate type,
    as it cannot be used here.
    @return the JSR 77 J2EEDomain.
     */
    @ManagedAttribute
    public AMXProxy getJ2EEDomain();

    /**
    Get the DomainConfig.
    For module dependency reasons, the returned object must be converted (if desired)
    to DomainConfig using getDomain().as(DomainConfig.class).
    @return the singleton DomainConfig
     */
    @ManagedAttribute
    public AMXConfigProxy getDomain();

    /**
    @return the singleton {@link MonitoringRoot}.
     */
    @ManagedAttribute
    @Description("Get the root MBean of all monitoring MBeans")
    public MonitoringRoot getMonitoringRoot();

    @ManagedAttribute
    @Description("Get the root MBean of all runtime MBeans")
    public RuntimeRoot getRuntime();

    /**
    @return the singleton SystemInfo
     */
    @ManagedAttribute
    public SystemInfo getSystemInfo();

    /**
    Notification type for JMX Notification issued when AMX MBeans are loaded
    and ready for use.
    @see #getAMXReady
     */
    public static final String AMX_READY_NOTIFICATION_TYPE =
            AMX.NOTIFICATION_PREFIX + "DomainRoot" + ".AMXReady";

    /**
    Poll to see if AMX is ready for use. It is more efficient to instead listen
    for a Notification of type {@link #AMX_READY_NOTIFICATION_TYPE}.  That
    should be done  by first registering the listener, then checking
    just after registration in case the Notification was issued in the ensuing
    interval just before the listener became registered.

    @return true if AMX is ready for use, false otherwise.
    @see #AMX_READY_NOTIFICATION_TYPE
     */
    @ManagedAttribute
    public boolean getAMXReady();

    /**
    Wait (block) until AMX is ready for use. Upon return, AMX is ready for use.
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    public void waitAMXReady();

    /**
    @since Glassfish V3
     */
    @ManagedAttribute
    public String getDebugPort();

    /**
    @since Glassfish V3
     */
    @ManagedAttribute
    public String getApplicationServerFullVersion();

    /**
    @since Glassfish V3
     */
    @ManagedAttribute
    public String getInstanceRoot();

    /**
    @return the directory for the domain
    @since Glassfish V3
     */
    @ManagedAttribute
    public String getDomainDir();

    /**
    @return the configuration directory, typically 'config' subdirectory of {@link #getDomainDir}
    @since Glassfish V3
     */
    @ManagedAttribute
    public String getConfigDir();

    /**
    @return the installation directory
    @since Glassfish V3
     */
    @ManagedAttribute
    @Description("the installation directory")
    public String getInstallDir();

    /**
    Return the time the domain admin server has been running.
    uptime[0] contains the time in milliseconds.  uptime[1] contains a human-readable
    string describing the duration.
     */
    @ManagedAttribute
    @Description("Return the time the domain admin server has been running.  uptime[0] contains the time in milliseconds.  uptime[1] contains a human-readable string describing the duration.")
    public Object[] getUptimeMillis();

    /**
        Return a Map of all non-compliant MBeans (MBeans might no longer be registered).
        The List&lt;String> contains all issues with that MBean.
        @since Glassfish V3
     */
    @ManagedAttribute
    @Description("Return a Map of all non-compliant MBeans (MBeans might no longer be registered).  The List&lt;String> contains all issues with that MBean")
    public Map<ObjectName, List<String>> getComplianceFailures();
}














