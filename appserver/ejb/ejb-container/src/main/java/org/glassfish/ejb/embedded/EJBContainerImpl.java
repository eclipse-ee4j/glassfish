/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.embedded;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.logging.LogDomains;

import jakarta.ejb.EJBException;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.transaction.TransactionManager;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * GlassFish implementation of the EJBContainer.
 *
 * @author Marina Vatkina
 */
public class EJBContainerImpl extends EJBContainer {

    // Use Bundle from another package
    private static final Logger _logger =
            LogDomains.getLogger(EjbContainerUtilImpl.class, LogDomains.EJB_LOGGER);

    private final GlassFish server;

    private final Deployer deployer;

    private String deployedAppName;

    private final ServiceLocator habitat;

    private volatile int state = STARTING;
    private final Cleanup cleanup;
    private DeploymentElement.ResultApplication res_app;

    private final static int STARTING = 0;
    private final static int RUNNING = 1;
    private final static int CLOSING = 2;
    private final static int CLOSED = 3;

    /**
     * Construct new EJBContainerImpl instance
     */
    EJBContainerImpl(GlassFish server) throws GlassFishException {
        this.server = server;
        this.server.start();

        this.habitat = server.getService(ServiceLocator.class);
        deployer = server.getDeployer();
        state = RUNNING;
        cleanup = new Cleanup(this);
    }

    /**
     * Construct new EJBContainerImpl instance and deploy found modules.
     */
    void deploy(Map<?, ?> properties, Set<DeploymentElement> modules) throws EJBException {
        try {
            String appName = (properties == null)? null : (String)properties.get(EJBContainer.APP_NAME);
            res_app = DeploymentElement.getOrCreateApplication(modules, appName);
            Object app = res_app.getApplication();

            if (app == null) {
                throw new EJBException("Invalid set of modules to deploy - see log for details");
            }

            if (_logger.isLoggable(Level.INFO)) {
                _logger.info("[EJBContainerImpl] Deploying app: " + app);
            }

            // Check if appName was set by application creation code
            appName = res_app.getAppName();

            String[] params;
            if (appName != null) {
                params = new String[] {"--name", appName};
            } else {
                params = new String[] {};
            }

            _logger.info("[EJBContainerImpl] GlassFish status: " + server.getStatus());
            if (app instanceof ScatteredArchive) {
                _logger.info("[EJBContainerImpl] Deploying as a ScatteredArchive");
                deployedAppName = deployer.deploy(((ScatteredArchive)app).toURI(), params);
            } else {
                _logger.info("[EJBContainerImpl] Deploying as a File");
                deployedAppName = deployer.deploy((File)app, params);
            }

        } catch (Exception e) {
            throw new EJBException("Failed to deploy EJB modules", e);
        }

        if (deployedAppName == null) {
            throw new EJBException("Failed to deploy EJB modules - see log for details");
        }
    }

    /**
     * Retrieve a naming context for looking up references to session beans
     * executing in the embeddable container.
     *
     * @return naming context
     */
    @Override
    public Context getContext() {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN getContext()");
        }
        try {
            return new InitialContext();
        } catch (Exception e) {
            throw new EJBException(_logger.getResourceBundle().getString(
                    "ejb.embedded.cannot_create_context"), e);
        }
    }

    /**
     * Shutdown an embeddable EJBContainer instance.
     */
    @Override
    public void close() {
        if (cleanup != null) {
            cleanup.disable();
        }
        if (isOpen()) {
            forceClose();
        }
    }

    void forceClose() {
        state = CLOSING;

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN close()");
        }

        undeploy();
        cleanupTransactions();
        cleanupConnectorRuntime();
        if (res_app != null && res_app.deleteOnExit()) {
            try {
                FileUtils.whack((File)res_app.getApplication());
            } catch (Exception e) {
                _logger.log(Level.WARNING, "Error in removing temp file", e);
            }
        }
        stop();
    }

    /**
     * Returns true if there are deployed modules associated with this container.
     */
    boolean isOpen() {
        return state == RUNNING;
    }

    private void cleanupTransactions() {
        try {
            /*
            Providers<TransactionManager> txProviders = habitat.forContract(TransactionManager.class);
            if (txProviders != null) {
                Provider<TransactionManager> provider = txProviders.getProvider();
                if (provider != null && provider.isActive()) {
                    TransactionManager txMgr = provider.get();
                    txMgr.rollback();
                }
            }
            */
            ServiceHandle<TransactionManager> inhabitant =
                    habitat.getServiceHandle(TransactionManager.class);
            if (inhabitant != null && inhabitant.isActive()) {
                TransactionManager txmgr = inhabitant.getService();
                if ( txmgr.getTransaction() != null ) {
                    txmgr.rollback();
                }
            }
        } catch (Throwable t) {
            _logger.log(Level.SEVERE, "Error in cleanupTransactions", t);
        }

    }

    private void cleanupConnectorRuntime() {
        try {
            /*
            Providers<ConnectorRuntime> txProviders = habitat.forContract(ConnectorRuntime.class);
            if (txProviders != null) {
                Provider<ConnectorRuntime> provider = txProviders.getProvider();
                if (provider != null && provider.isActive()) {
                    ConnectorRuntime connectorRuntime = provider.get();
                    connectorRuntime.cleanUpResourcesAndShutdownAllActiveRAs();
                }
            }
            */
            ServiceHandle<ConnectorRuntime> inhabitant =
                    habitat.getServiceHandle(ConnectorRuntime.class);
            if (inhabitant != null && inhabitant.isActive()) {
                ConnectorRuntime connectorRuntime = inhabitant.getService();
                connectorRuntime.cleanUpResourcesAndShutdownAllActiveRAs();
            }
        } catch (Throwable t) {
            _logger.log(Level.SEVERE, "Error in cleanupConnectorRuntime", t);
        }
    }

    private void undeploy() {
        if (deployedAppName != null) {
            try {
                deployer.undeploy(deployedAppName);
            } catch (Exception e) {
                _logger.warning("Cannot undeploy deployed modules: " + e.getMessage());
            }
        }
    }

    void stop() {
        if (state == CLOSED) {
            return;
        }
        try {
            server.stop();
        } catch (GlassFishException e) {
            _logger.log(Level.WARNING, "Cannot stop embedded server", e);
        } finally {
            try {
                server.dispose();
            } catch (GlassFishException e) {
                _logger.log(Level.WARNING, "Cannot dispose embedded server", e);
            }
            state = CLOSED;
        }
    }

    private static class Cleanup implements Runnable {

        private Thread cleanupThread = null;
        private EJBContainerImpl container = null;

        Cleanup(EJBContainerImpl container) {
            this.container = container;
            Runtime.getRuntime().addShutdownHook(
                    cleanupThread = new Thread(this, "GlassFish EJBContainerImpl Cleanup Shutdown Hook"));
        }

        void disable() {
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    @Override
                    public Object run() {
                        Runtime.getRuntime().removeShutdownHook(cleanupThread);
                        return null;
                    }
                }
            );
        }

        @Override
        public void run() {
            if (container.isOpen()) {
                container.forceClose();
            }
        }
    }
}
