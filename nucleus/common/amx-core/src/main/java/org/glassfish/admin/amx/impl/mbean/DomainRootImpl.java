/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.amx.impl.mbean;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.universal.Duration;
import com.sun.enterprise.universal.io.SmartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.glassfish.admin.amx.base.BulkAccess;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.base.Ext;
import org.glassfish.admin.amx.base.Pathnames;
import org.glassfish.admin.amx.base.Query;
import org.glassfish.admin.amx.base.Realms;
import org.glassfish.admin.amx.base.RuntimeRoot;
import org.glassfish.admin.amx.base.Sample;
import org.glassfish.admin.amx.base.Tools;
import org.glassfish.admin.amx.core.AMXValidator;
import org.glassfish.admin.amx.impl.util.InjectedValues;
import org.glassfish.admin.amx.impl.util.Issues;
import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.logging.Logging;
import org.glassfish.admin.amx.monitoring.MonitoringRoot;
import org.glassfish.admin.amx.util.AMXLoggerInfo;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.FeatureAvailability;
import org.glassfish.admin.amx.util.MapUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.server.ServerEnvironmentImpl;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;

/**
 */
public class DomainRootImpl extends AMXImplBase // implements DomainRoot
{

    private final String mAppserverDomainName;
    private final File mInstanceRoot;
    private volatile ComplianceMonitor mCompliance;

    private static final Logger logger = AMXLoggerInfo.getLogger();

    public DomainRootImpl() {
        super(null, DomainRoot.class);
        mInstanceRoot = new File(System.getProperty(INSTANCE_ROOT.getSystemPropertyName()));
        mAppserverDomainName = mInstanceRoot.getName();
    }

    public void stopDomain() {
        getDomainRootProxy().getRuntime().stopDomain();
    }

    public ObjectName getQueryMgr() {
        return child(Query.class);
    }

    public ObjectName getJ2EEDomain() {
        return child("J2EEDomain");
    }

    public ObjectName getMonitoringRoot() {
        return child(MonitoringRoot.class);
    }

    public ObjectName getPathnames() {
        return child(Pathnames.class);
    }

    public ObjectName getBulkAccess() {
        return child(BulkAccess.class);
    }

    @Override
    protected ObjectName preRegisterHook(final MBeanServer server,
            final ObjectName selfObjectName)
            throws Exception {
        // DomainRoot has not yet been registered; any MBeans that exist are non-compliant
        // because they cannot have a Parent.
        final Set<ObjectName> existing = JMXUtil.queryAllInDomain(server, selfObjectName.getDomain());
        if (existing.size() != 0) {
            logger.log(Level.INFO, AMXLoggerInfo.mbeanExist, CollectionUtil.toString(existing, ", "));
        }

        return selfObjectName;
    }

    @Override
    public void preRegisterDone()
            throws Exception {
        super.preRegisterDone();
    }

    @Override
    protected void postRegisterHook(final Boolean registrationSucceeded) {
        super.postRegisterHook(registrationSucceeded);

        // Start compliance after everything else; it uses key MBeans like Paths
        //turning off ComplianceMonitor for now to help embedded runs.
        if (registrationSucceeded.booleanValue()) {
            // start compliance monitoring immediately, even before children are registered
            mCompliance = ComplianceMonitor.getInstance(getDomainRootProxy());
            mCompliance.start();
        }
    }

    public Map<ObjectName, List<String>> getComplianceFailures() {
        final Map<ObjectName, List<String>> result = MapUtil.newMap();
        if (mCompliance == null) {
            return result;
        }
        final Map<ObjectName, AMXValidator.ProblemList> failures =
                mCompliance.getComplianceFailures();

        for (final Map.Entry<ObjectName, AMXValidator.ProblemList> me : failures.entrySet()) {
            result.put(me.getKey(), me.getValue().getProblems());
        }

        return result;
    }

    public String getAppserverDomainName() {
        return (mAppserverDomainName);
    }

    @Override
    protected final void registerChildren() {
        super.registerChildren();
        //System.out.println("Registering children of DomainRoot");
        final ObjectName self = getObjectName();
        final ObjectNameBuilder objectNames =
                new ObjectNameBuilder(getMBeanServer(), self);

        ObjectName childObjectName = null;
        Object mbean = null;
        final MBeanServer server = getMBeanServer();

        /**
        Follow this order: some later MBeans might depend on others.
         */
        childObjectName = objectNames.buildChildObjectName(Pathnames.class);
        mbean = new PathnamesImpl(self);
        registerChild(mbean, childObjectName);

        childObjectName = objectNames.buildChildObjectName(Query.class);
        mbean = new QueryMgrImpl(self);
        registerChild(mbean, childObjectName);

        childObjectName = objectNames.buildChildObjectName(Logging.class);
        mbean = new LoggingImpl(self, "server");
        registerChild(mbean, childObjectName);

        childObjectName = objectNames.buildChildObjectName(Tools.class);
        mbean = new ToolsImpl(self);
        registerChild(mbean, childObjectName);

        childObjectName = objectNames.buildChildObjectName(BulkAccess.class);
        mbean = new BulkAccessImpl(self);
        registerChild(mbean, childObjectName);

        childObjectName = objectNames.buildChildObjectName(Sample.class);
        mbean = new SampleImpl(self);
        registerChild(mbean, childObjectName);

        childObjectName = objectNames.buildChildObjectName(RuntimeRoot.class);
        mbean = new RuntimeRootImpl(self);
        registerChild(mbean, childObjectName);

        // after registering Ext, other MBeans can depend on the above ones egs Paths, Query
        childObjectName = objectNames.buildChildObjectName(Ext.class);
        final ObjectName extObjectName = childObjectName;
        mbean = new ExtImpl(self);
        registerChild(mbean, childObjectName);

        childObjectName = ObjectNameBuilder.buildChildObjectName(server, extObjectName, Realms.class);
        mbean = new RealmsImpl(extObjectName);
        registerChild(mbean, childObjectName);

        // Monitoring MBeans can rely on all the prior MBeans
        childObjectName = objectNames.buildChildObjectName(MonitoringRoot.class);
        mbean = new MonitoringRootImpl(self);
        registerChild(mbean, childObjectName);
    }

    public boolean getAMXReady() {
        // just block until ready, no need to support polling
        waitAMXReady();
        return true;
    }

    public void waitAMXReady() {
        FeatureAvailability.getInstance().waitForFeature(FeatureAvailability.AMX_READY_FEATURE, this.getClass().getName());
    }

    public String getDebugPort() {
        Issues.getAMXIssues().notDone("DomainRootImpl.getDebugPort");
        return "" + 9999;
    }

    public String getApplicationServerFullVersion() {
        return Version.getProductIdInfo();
    }

    public String getInstanceRoot() {
        return SmartFile.sanitize(System.getProperty(INSTANCE_ROOT.getSystemPropertyName()));
    }

    public String getDomainDir() {
        return SmartFile.sanitize(mInstanceRoot.toString());
    }

    public String getConfigDir() {
        return getDomainDir() + "/" + "config";
    }

    public String getInstallDir() {
        return SmartFile.sanitize(System.getProperty(INSTALL_ROOT.getSystemPropertyName()));
    }

    public Object[] getUptimeMillis() {
        final ServerEnvironmentImpl env = InjectedValues.getInstance().getServerEnvironment();

        final long elapsed = System.currentTimeMillis() - env.getStartupContext().getCreationTime();
        final Duration duration = new Duration(elapsed);

        return new Object[]{
                    elapsed, duration.toString()
                };
    }
}












