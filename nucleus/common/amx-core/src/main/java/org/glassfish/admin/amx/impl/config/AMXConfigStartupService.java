/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.impl.config;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.util.logging.Level;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.impl.util.InjectedValues;
import org.glassfish.admin.amx.util.AMXLoggerInfo;
import org.glassfish.admin.amx.util.TimingDelta;
import org.glassfish.admin.mbeanserver.PendingConfigBeans;
import org.glassfish.api.amx.AMXLoader;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Transactions;

/**
Startup service that loads support for AMX config MBeans.  How this is to be
triggered is not yet clear.
 */
@Taxonomy(stability = Stability.NOT_AN_INTERFACE)
@Service
public final class AMXConfigStartupService
        implements org.glassfish.hk2.api.PostConstruct,
        org.glassfish.hk2.api.PreDestroy,
        AMXLoader {

    @Inject
    InjectedValues mInjectedValues;
    @Inject//(name=AppserverMBeanServerFactory.OFFICIAL_MBEANSERVER)
    private MBeanServer mMBeanServer;
    @Inject
    private volatile PendingConfigBeans mPendingConfigBeans;
    @Inject
    private Transactions mTransactions;
    private volatile AMXConfigLoader mLoader;
    private volatile PendingConfigBeans mPendingConfigBeansBackup;

    public AMXConfigStartupService() {
        //debug( "AMXStartupService.AMXStartupService()" );
    }

    public void postConstruct() {
        final TimingDelta delta = new TimingDelta();

        if (mMBeanServer == null) {
            throw new Error("AMXStartup: null MBeanServer");
        }
        if (mPendingConfigBeans == null) {
            throw new Error("AMXStartup: null mPendingConfigBeans");
        }

        mPendingConfigBeansBackup = mPendingConfigBeans;
        AMXLoggerInfo.getLogger().log(Level.FINE, "Initialized AMXConfig Startup service in {0} ms", delta.elapsedMillis());
    }

    public void preDestroy() {
        AMXLoggerInfo.getLogger().info(AMXLoggerInfo.stoppingAMX);
        unloadAMXMBeans();
    }

    public DomainRoot getDomainRoot() {
        return ProxyFactory.getInstance(mMBeanServer).getDomainRootProxy(false);
    }

    public ObjectName getDomainConfig() {
            return ConfigBeanRegistry.getInstance().getObjectNameForProxy(getDomain());
    }

    public Domain getDomain() {
            return InjectedValues.getInstance().getHabitat().getService(Domain.class);
    }

    public AMXProxy getDomainConfigProxy() {
        return ProxyFactory.getInstance(mMBeanServer).getProxy(getDomainConfig(), AMXProxy.class);
    }

    public synchronized ObjectName loadAMXMBeans() {
        if (mLoader == null) {
            //getDomainRootProxy().waitAMXReady();
            if(mPendingConfigBeans.size() == 0)  {
                mPendingConfigBeans = mPendingConfigBeansBackup;
            }
            mLoader = new AMXConfigLoader(mMBeanServer, mPendingConfigBeans, mTransactions);
            mLoader.start();
            // asynchronous start, caller must wait for
        }
        return getDomainConfig();
    }

    public synchronized void unloadAMXMBeans() {
        final AMXProxy domainConfigProxy = getDomainConfigProxy();
        if (domainConfigProxy != null) {
                ImplUtil.unregisterAMXMBeans(domainConfigProxy);
        }
        if (mLoader != null) {
            mLoader.stop();
        }
        mLoader = null;
    }
}
