/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

import com.sun.corba.ee.impl.folb.InitialGroupInfoService;
import com.sun.enterprise.deployment.EjbDescriptor;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.System.Logger;
import java.rmi.Remote;
import java.util.Properties;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.internal.api.ORBLocator;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ServerRequestInfo;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static org.glassfish.api.event.EventTypes.SERVER_SHUTDOWN;

/**
 * This class exposes any orb/iiop functionality needed by modules in the app server.
 * This prevents modules from needing any direct dependencies on the orb-iiop module.
 *
 * @author Mahesh Kannan, Jan 17, 2009
 */
@Service
@Singleton
public class GlassFishORBLocator implements ORBLocator {

    private static final Logger LOG = System.getLogger(GlassFishORBLocator.class.getName());
    private static final ThreadLocal<ORB> TMP_ORB = new ThreadLocal<>();

    @Inject
    private ProcessEnvironment environment;

    @Inject
    private Provider<Events> eventsProvider;

    @Inject
    private Provider<ProtocolManager> protocolManagerProvider;

    @Inject
    private Provider<GlassfishNamingManager> glassfishNamingManagerProvider;

    @Inject
    private GlassFishORBFactory orbFactory;

    private ProtocolManager protocolManager;

    // volatile is enough for sync, just one thread can write
    private volatile ORB orb;
    private volatile boolean destroyed;
    private volatile Exception failure;
    private Thread initializerThread;

    @PostConstruct
    private void postConstruct() {
        // WARN: Neither PreDestroy annotation nor interface worked!
        EventListener glassfishEventListener = event -> {
            if (event.is(SERVER_SHUTDOWN)) {
                onShutdown();
            }
        };
        eventsProvider.get().register(glassfishEventListener);
        getORB();
        LOG.log(INFO, "GlassFishORBLocator created.");
    }

    private synchronized void onShutdown() {
        destroyed = true;
        LOG.log(INFO, "GlassFishORBLocator shutdown started");
        if (this.orb != null) {
            // First remove, then destroy.
            // Still, threads already working with the instance will have it unstable.
            final ORB destroyedOrb = orb;
            orb = null;
            // FIXME: com.sun.corba.ee.impl.transport.AcceptorImpl.getAcceptedSocket(AcceptorImpl.java:127)
            //        can still be blocked in standalone thread, that would lead to its failure
            //        and cascade leading sockets open. Restart of the server could fail then.
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                // We don't want to interrupt here.
            }
            destroyedOrb.destroy();
        }
    }

    @Override
    public ORB getORB() {
        LOG.log(TRACE, "getORB() entry");
        if (destroyed) {
            return null;
        }
        if (isORBInitialized()) {
            LOG.log(TRACE, "getORB() the shortest path out");
            return orb;
        }

        synchronized (this) {
            if (Thread.currentThread() == initializerThread) {
                // Awful design of corba-orb library needs this.
                LOG.log(DEBUG, "Detected recursion!", new Exception());
                return TMP_ORB.get();
            }
            if (this.orb != null || destroyed) {
                LOG.log(TRACE, "getORB() second shortest path out");
                return orb;
            }
            if (this.failure != null) {
                throw new RuntimeException("ORB initialization already failed in another thread!", failure);
            }
            // First thread will create the ORB.
            initializerThread = Thread.currentThread();
            try {
                try {
                    initialize(environment.getProcessType().isServer());
                    LOG.log(DEBUG, "ORB initialization finished successfuly.");
                    return orb;
                } catch (Exception e) {
                    failure = e;
                    // Close all ports, etc.
                    if (orb != null) {
                        orb.destroy();
                    }
                }
                // Current stacktrace + original cause.
                throw new RuntimeException("ORB initialization failed!", failure);
            } finally {
                TMP_ORB.remove();
                initializerThread = null;
            }
        }
    }

    private void initialize(boolean isServer) throws Exception {
        LOG.log(DEBUG, "getORB(): Initialization started by " + Thread.currentThread(), new Exception());
        final Properties props = new Properties();
        props.setProperty(GlassFishORBFactory.ENV_IS_SERVER_PROPERTY, Boolean.toString(isServer));
        this.orb = orbFactory.createORB(props);
        if (isServer) {
            // Does the JNDI binding
            new InitialGroupInfoService(orb);
        }
        LOG.log(INFO, "ORB initialization succeeded: {0}", orb);
    }

    private ProtocolManager initProtocolManager() throws Exception {
        if (failure != null) {
            throw new IllegalStateException(
                "Cannot initialize the protocol manager, because ORB initialization failed.", failure);
        }
        final ProtocolManager manager = protocolManagerProvider.get();
        manager.initialize(orb);

        // Move startup of naming to PEORBConfigurator so it runs before interceptors.
        manager.initializePOAs();

        final GlassfishNamingManager namingManager = glassfishNamingManagerProvider.get();
        final Remote remoteSerialProvider = namingManager.initializeRemoteNamingSupport(orb);
        manager.initializeRemoteNaming(remoteSerialProvider);
        return manager;
    }

    /**
     * Get a protocol manager for creating remote references.
     * ProtocolManager is only available in the server.
     * Otherwise, this method returns null.
     */
    public ProtocolManager getProtocolManager() {
        if (!environment.getProcessType().isServer() || destroyed) {
            return null;
        }
        if (!isORBInitialized()) {
            getORB();
        }
        if (protocolManager != null) {
            return protocolManager;
        }

        synchronized (this) {
            try {
                if (environment.getProcessType().isServer()) {
                    protocolManager = initProtocolManager();
                    LOG.log(INFO, "ProtocolManager initialization finished successfuly.");
                }
                return protocolManager;
            } catch (Exception e) {
                throw new IllegalStateException("ProtocolManager initialization failed!", e);
            }
        }
    }

    /**
     * @return true if the orb is not null and the initialization already finished.
     */
    public boolean isORBInitialized() {
        return orb != null && initializerThread == null;
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

    public EjbDescriptor getEjbDescriptor(IORInfo iorInfo) {
        return orbFactory.getEjbDescriptor(iorInfo);
    }

    @Override
    public int getORBPort() {
        while (!isORBInitialized()) {
            Thread.onSpinWait();
        }
        return orbFactory.getORBPort(orb);
    }

    public boolean isEjbCall(ServerRequestInfo sri) {
        return orbFactory.isEjbCall(sri);
    }

    /**
     * Due to bad corba-orb design, we need to have a partially initialized {@link ORB} instance
     * ready for recursive requests.
     *
     * @param orb
     */
    public static void setThreadLocal(ORB orb) {
        TMP_ORB.set(orb);
    }
}
