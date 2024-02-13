/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.enterprise.iiop.api;

import com.sun.logging.LogDomains;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.nio.channels.SelectableChannel;
import java.rmi.Remote;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ORBLocator;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.ServerRequestInfo;

import static com.sun.logging.LogDomains.CORBA_LOGGER;

/**
 * This class exposes any orb/iiop functionality needed by modules in the app server.
 * This prevents modules from needing any direct dependencies on the orb-iiop module.
 *
 * @author Mahesh Kannan, Jan 17, 2009
 */
@Service
@Singleton
public class GlassFishORBHelper implements ORBLocator {

    private static final Logger LOG = LogDomains.getLogger(GlassFishORBHelper.class, CORBA_LOGGER, false);

    @Inject
    private ServiceLocator services;

    @Inject
    private ProcessEnvironment processEnv;

    @Inject
    private Provider<ProtocolManager> protocolManagerProvider;

    @Inject
    private Provider<GlassfishNamingManager> glassfishNamingManagerProvider;

    private ProtocolManager protocolManager;
    private SelectableChannelDelegate selectableChannelDelegate;
    private GlassFishORBFactory orbFactory;

    // volatile is enough for sync, just one thread can write
    private volatile ORB orb;
    private volatile boolean destroyed;

    @PostConstruct
    public void postConstruct() {
        orbFactory = services.getService(GlassFishORBFactory.class);
    }

    @PreDestroy
    public void onShutdown() {
        // FIXME: getORB is able to create another, it should be refactored and simplified.
        destroyed = true;
        LOG.log(Level.CONFIG, "ORB Shutdown started");
        if (orb != null) {
            orb.destroy();
            orb = null;
        }
    }

    // Called by PEORBConfigurator executed from glassfish-corba-orb.jar
    public synchronized void setORB(ORB orb) {
        this.orb = orb;
    }

    /**
     * Get or create the default orb. This can be called for any process type. However, protocol manager and CosNaming
     * initialization only take place for the Server.
     */
    @Override
    public ORB getORB() {
        // Use a volatile double-checked locking idiom here so that we can publish
        // a partly-initialized ORB early, so that lazy init can come into getORB()
        // and allow an invocation to the transport to complete.
        {
            final ORB orbInstance = this.orb;
            if (orbInstance != null || destroyed) {
                return orbInstance;
            }
        }
        synchronized (this) {
            if (this.orb != null) {
                return this.orb;
            }
            try {
                final boolean isServer = processEnv.getProcessType().isServer();

                final Properties props = new Properties();
                props.setProperty(GlassFishORBFactory.ENV_IS_SERVER_PROPERTY, Boolean.toString(isServer));

                // Create orb and make it visible.
                //
                // This will allow loopback calls to getORB() from portable interceptors activated
                // as a side-effect of the remaining initialization.
                //
                // If it's a server, there's a small time window during which the ProtocolManager
                // won't be available.
                // Any callbacks that result from the protocol manager initialization itself cannot
                // depend on having access to the protocol manager.
                orb = orbFactory.createORB(props);
                if (isServer && protocolManager == null) {
                    protocolManager = initProtocolManager(orb);
                }
                return orb;
            } catch (Exception e) {
                orb = null;
                protocolManager = null;
                throw new RuntimeException("Orb initialization erorr", e);
            }
        }
    }


    private ProtocolManager initProtocolManager(final ORB orbInstance) throws Exception {
        final ProtocolManager manager = protocolManagerProvider.get();
        manager.initialize(orbInstance);

        // Move startup of naming to PEORBConfigurator so it runs before interceptors.
        manager.initializePOAs();

        final GlassfishNamingManager namingManager = glassfishNamingManagerProvider.get();
        final Remote remoteSerialProvider = namingManager.initializeRemoteNamingSupport(orbInstance);
        manager.initializeRemoteNaming(remoteSerialProvider);
        return manager;
    }

    public void setSelectableChannelDelegate(SelectableChannelDelegate d) {
        selectableChannelDelegate = d;
    }

    public SelectableChannelDelegate getSelectableChannelDelegate() {
        return this.selectableChannelDelegate;
    }


    /**
     * Get a protocol manager for creating remote references.
     * ProtocolManager is only available in the server.
     * Otherwise, this method returns null.
     * If it's the server and the orb hasn't been already created, calling this method has the side
     * effect of creating the orb.
     */
    public ProtocolManager getProtocolManager() {
        if (!processEnv.getProcessType().isServer() || destroyed) {
            return null;
        }
        synchronized (this) {
            if (protocolManager == null) {
                getORB();
            }
            return protocolManager;
        }
    }

    public boolean isORBInitialized() {
        return orb != null;
    }

    public int getOTSPolicyType() {
        return orbFactory.getOTSPolicyType();
    }

    public int getCSIv2PolicyType() {
        return orbFactory.getCSIv2PolicyType();
    }

    public Properties getCSIv2Props() {
        return orbFactory.getCSIv2Props();
    }

    public void setCSIv2Prop(String name, String value) {
        orbFactory.setCSIv2Prop(name, value);
    }

    public int getORBInitialPort() {
        return orbFactory.getORBInitialPort();
    }

    @Override
    public String getORBHost(ORB orb) {
        return orbFactory.getORBHost(orb);
    }

    @Override
    public int getORBPort(ORB orb) {
        return orbFactory.getORBPort(orb);
    }

    public boolean isEjbCall(ServerRequestInfo sri) {
        return orbFactory.isEjbCall(sri);
    }

    public interface SelectableChannelDelegate {
        void handleRequest(SelectableChannel channel);
    }
}
