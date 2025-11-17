/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.mbeanserver;

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.JmxConnector;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.external.amx.BootAMXMBean;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.jvnet.hk2.annotations.Service;

/**
 * Responsible for creating the {@link BootAMXMBean}, and starting JMXConnectors,
 * which will initialize (boot) AMX when a connection arrives.
 */
@Service
@RunLevel(mode=RunLevel.RUNLEVEL_MODE_NON_VALIDATING, value=PostStartupRunLevel.VAL)
public final class JMXStartupService implements PostConstruct {

    static final String JMX_CONNECTOR_SERVER_PREFIX = "jmxremote:type=jmx-connector-server";

    private static final Logger LOG = Util.JMX_LOGGER;
    private static final ServiceLocator LOCATOR = Globals.getDefaultHabitat();


    @Inject
    private MBeanServer mMBeanServer;
    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private AdminService mAdminService;
    @Inject
    private ServiceLocator locator;
    @Inject
    private Events mEvents;


    @Inject
    private ServerEnvironment serverEnv;

    public enum JMXConnectorStatus {STOPPED, STARTED};

    private volatile JMXConnectorStatus jmxConnectorstatus = JMXConnectorStatus.STOPPED;
    private Object lock = new Object();

    private volatile BootAMX mBootAMX;
    private volatile JMXConnectorsStarterThread mConnectorsStarterThread;


    @LogMessageInfo(message = "JMXStartupService and JMXConnectors have been shut down.", level="INFO")
    private static final String JMX_STARTUPSERVICE_SHUTDOWN=Util.LOG_PREFIX + "00001";

    @LogMessageInfo(message="JMXStartupService: Stopped JMXConnectorServer: {0}", level="INFO")
    private static final String JMX_STARTUPSERVICE_STOPPED_JMX_CONNECTOR=Util.LOG_PREFIX + "00002";

    @LogMessageInfo(message="MBean Registration Exception thrown {0}", level="SEVERE",
            cause="JMX Connector Server MBean could not be unregistered.",
            action="Take appropriate action based on the exception message.")
    private static final String JMX_MBEAN_REG_EXCEPTION=Util.LOG_PREFIX + "00003";

    @LogMessageInfo(message="Instance Not Found Exception thrown {0}", level="SEVERE",
            cause="JMX Connector Server MBean instance not found.",
            action="Take appropriate action based on the exception message.")
    private static final String JMX_INSTANCE_NOT_FOUND_EXCEPTION=Util.LOG_PREFIX + "00004";

    @LogMessageInfo(message = "JMXStartupService has started JMXConnector on JMXService URL {0}", level="INFO")
    private static final String JMX_STARTED_SERVICE=Util.LOG_PREFIX + "00005";

    @LogMessageInfo(message = "JMXStartupService has disabled JMXConnector {0}", level="INFO")
    private static final String JMX_STARTED_SERVICE_DISABLED=Util.LOG_PREFIX + "00006";

    public JMXStartupService() {
        mMBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    private final class ShutdownListener implements EventListener {

        @Override
        public void event(Event<?> event) {
            if (event.is(EventTypes.PREPARE_SHUTDOWN)) {
                shutdown();
            }
        }
    }

    public void waitUntilJMXConnectorStarted() {
        synchronized (lock) {
            while (jmxConnectorstatus != JMXConnectorStatus.STARTED) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {}
            }
        }
    }

    @Override
    public void postConstruct() {
        mBootAMX = BootAMX.create(locator, mMBeanServer);

        final List<JmxConnector> configuredConnectors = mAdminService.getJmxConnector();

        final boolean autoStart = false;

        mConnectorsStarterThread = new JMXConnectorsStarterThread(
            AdminAuthorizedMBeanServer.newInstance(mMBeanServer, serverEnv.isInstance(), mBootAMX),
            configuredConnectors, mBootAMX, !autoStart, this);
        mConnectorsStarterThread.start();

        // start AMX *first* (if auto start) so that it's ready
        if (autoStart) {
            new BootAMXThread(mBootAMX).start();
        }

        mEvents.register(new ShutdownListener());
    }

    private synchronized void shutdown() {
        LOG.fine("JMXStartupService: shutting down AMX and JMX");

        if (mBootAMX != null) {
            mBootAMX.shutdown();
        }
        mBootAMX = null;

        if (mConnectorsStarterThread != null) {
            mConnectorsStarterThread.shutdown();
        }
        mConnectorsStarterThread = null;

        if (javax.management.MBeanServerFactory.findMBeanServer(null).size() > 0) {
            MBeanServer server = javax.management.MBeanServerFactory.findMBeanServer(null).get(0);
            javax.management.MBeanServerFactory.releaseMBeanServer(server);
        }

        // we can't block here waiting, we have to assume that the rest of the AMX modules do the right thing
        LOG.log(Level.INFO, JMX_STARTUPSERVICE_SHUTDOWN);


    }

    private static final class BootAMXThread extends Thread {

        private final BootAMX mBooter;

        private BootAMXThread(final BootAMX booter) {
            mBooter = booter;
        }

        @Override
        public void run() {
            try {
                mBooter.bootAMX();
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "JMX Boot failed!", t);
            }
        }
    }

    /**
     * Thread that starts the configured JMXConnectors.
     */
    private static final class JMXConnectorsStarterThread extends Thread {

        private final List<JmxConnector> mConfiguredConnectors;
        private final MBeanServer mMBeanServer;
        private final BootAMX mAMXBooterNew;
        private final boolean mNeedBootListeners;
        ConnectorStarter starter;
        ObjectName connObjectName;
        JMXStartupService service;

        private JMXConnectorsStarterThread(
                final MBeanServer mbs,
                final List<JmxConnector> configuredConnectors,
                final BootAMX amxBooter,
                final boolean needBootListeners,
                JMXStartupService service) {
            mMBeanServer = mbs;
            mConfiguredConnectors = configuredConnectors;
            mAMXBooterNew = amxBooter;
            mNeedBootListeners = needBootListeners;
            this.service = service;
        }

        void shutdown() {
            if (starter != null && starter instanceof RMIConnectorStarter) {
                ((RMIConnectorStarter) starter).stopAndUnexport();
            }
            try {
                if (connObjectName != null) {
                    mMBeanServer.unregisterMBean(connObjectName);
                    connObjectName = null;
                }
            } catch (MBeanRegistrationException ex) {
                LOG.log(Level.SEVERE, JMX_MBEAN_REG_EXCEPTION, ex);
            } catch (InstanceNotFoundException ex) {
                LOG.log(Level.SEVERE, JMX_INSTANCE_NOT_FOUND_EXCEPTION, ex);
            }
            synchronized (service.lock) {
                for (final JMXConnectorServer connector : mConnectorServers) {
                    try {
                        final JMXServiceURL address = connector.getAddress();
                        connector.stop();
                        LOG.log(Level.INFO, JMX_STARTUPSERVICE_STOPPED_JMX_CONNECTOR, address);
                    } catch (final Exception e) {
                        LOG.log(Level.SEVERE, "Failed to stop connector " + connector, e);
                    }
                }
                service.jmxConnectorstatus = JMXConnectorStatus.STOPPED;
            }
            mConnectorServers.clear();
        }

        private static String toString(final JmxConnector c) {
            return "JmxConnector config: { name = " + c.getName() +
                    ", Protocol = " + c.getProtocol() +
                    ", Address = " + c.getAddress() +
                    ", Port = " + c.getPort() +
                    ", AcceptAll = " + c.getAcceptAll() +
                    ", AuthRealmName = " + c.getAuthRealmName() +
                    ", SecurityEnabled = " + c.getSecurityEnabled() +
                    "}";
        }

        private JMXConnectorServer startConnector(final JmxConnector connConfig) throws IOException {
            LOG.log(Level.FINE, () -> "Starting JMXConnector: " + toString(connConfig));

            final String protocol = connConfig.getProtocol();
            final String address = connConfig.getAddress();
            final int port = Integer.parseInt(connConfig.getPort());
            final boolean securityEnabled = Boolean.parseBoolean(connConfig.getSecurityEnabled());
            final Ssl ssl = connConfig.getSsl();

            JMXConnectorServer server = null;
            final BootAMXListener listener = mNeedBootListeners ?
                    new BootAMXListener(mAMXBooterNew) : null;
            if (protocol.equals("rmi_jrmp")) {
                starter = new RMIConnectorStarter(mMBeanServer, address, port,
                        protocol, securityEnabled, LOCATOR,
                        listener, ssl);
                server = ((RMIConnectorStarter) starter).start();
            } else {
                throw new IllegalArgumentException("JMXStartupService.startConnector(): Unknown protocol: " + protocol);
            }
            if (listener != null) {
                listener.setServer(server);
            }
            final JMXServiceURL url = server.getAddress();
            LOG.log(Level.INFO, JMX_STARTED_SERVICE, url);

            try {
                connObjectName = new ObjectName(JMX_CONNECTOR_SERVER_PREFIX + ",protocol=" + protocol + ",name=" + connConfig.getName());
                mMBeanServer.registerMBean(server, connObjectName).getObjectName();
            } catch (final Exception e) {
                // it's not critical to have it registered as an MBean
                LOG.log(Level.SEVERE, "Failed to register mbean.", e);
            }

            return server;
        }

        private final List<JMXConnectorServer> mConnectorServers = new ArrayList<>();

        @Override
        public void run() {
            synchronized (service.lock) {
                for (final JmxConnector c : mConfiguredConnectors) {
                    if (!Boolean.parseBoolean(c.getEnabled())) {
                        LOG.log(Level.INFO, JMX_STARTED_SERVICE_DISABLED, c.getName());
                        continue;
                    }

                    try {
                        final JMXConnectorServer server = startConnector(c);
                        mConnectorServers.add(server);
                    } catch (final Throwable t) {
                        LOG.log(Level.WARNING, "Cannot start JMX connector " + c, t);
                    }
                }
                service.jmxConnectorstatus = JMXConnectorStatus.STARTED;
                service.lock.notifyAll();
            }
        }
    }

    public static final Set<ObjectName> getJMXConnectorServers(final MBeanServer server) {
        try {
            final ObjectName queryPattern = new ObjectName(JMX_CONNECTOR_SERVER_PREFIX + ",*");
            return server.queryNames(queryPattern, null);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the JMXServiceURLs for all connectors we've loaded.
     */
    public static JMXServiceURL[] getJMXServiceURLs(final MBeanServer server) {
        final Set<ObjectName> objectNames = getJMXConnectorServers(server);

        final List<JMXServiceURL> urls = new ArrayList<JMXServiceURL>();
        for (final ObjectName objectName : objectNames) {
            try {
                urls.add((JMXServiceURL) server.getAttribute(objectName, "Address"));
            } catch (JMException e) {
                throw new RuntimeException(e);
            }
        }

        return urls.toArray(JMXServiceURL[]::new);
    }
}
