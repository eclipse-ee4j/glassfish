/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.internal.data;

import com.sun.enterprise.config.serverbeans.ApplicationConfig;
import com.sun.enterprise.config.serverbeans.Engine;

import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.config.TransactionFailure;

import static java.util.logging.Level.WARNING;

/**
 * When a module is attached to a LoadedEngine, it creates an Engine reference. Each module of an application can be
 * loaded in several engines, however, a particular module can only be loaded once in a particular engine.
 *
 * @author Jerome Dochez
 */
public class EngineRef {

    final private EngineInfo<?, ?> engineInfo;
    private ApplicationContainer<?> applicationContainer;
    private ApplicationConfig appConfig;

    public EngineRef(EngineInfo<?, ?> container, ApplicationContainer<?> appCtr) {
        this.engineInfo = container;
        this.applicationContainer = appCtr;
    }

    /**
     * Returns the container associated with this application
     *
     * @return the container for this application
     */
    public EngineInfo<?, ?> getContainerInfo() {
        return engineInfo;
    }

    /**
     * Set the contaier associated with this application
     *
     * @param applicationContainer the container for this application
     */
    public void setApplicationContainer(ApplicationContainer<?> applicationContainer) {
        this.applicationContainer = applicationContainer;
    }

    /**
     * Returns the contaier associated with this application
     *
     * @return the container for this application
     */
    public ApplicationContainer<?> getApplicationContainer() {
        return applicationContainer;
    }

    public void setApplicationConfig(final ApplicationConfig config) {
        appConfig = config;
    }

    public ApplicationConfig getApplicationConfig() {
        return appConfig;
    }

    public void load(ExtendedDeploymentContext context, ProgressTracker tracker) {
        getContainerInfo().load(context);
        tracker.add("loaded", EngineRef.class, this);
    }

    public boolean start(ApplicationContext context, ProgressTracker tracker) throws Exception {
        if (applicationContainer == null) {
            // the container does not care to be started or stopped
            return true;
        }

        if (!applicationContainer.start(context)) {
            return false;
        }

        tracker.add("started", EngineRef.class, this);
        return true;
    }

    /**
     * unloads the module from its container.
     *
     * @param context unloading context
     * @return
     */
    public boolean unload(ExtendedDeploymentContext context) {
        ActionReport report = context.getActionReport();

        // then remove the application from the container
        Deployer deployer = engineInfo.getDeployer();
        try {
            deployer.unload(applicationContainer, context);
            engineInfo.unload(context);
        } catch (Exception e) {
            report.failure(context.getLogger(), "Exception while shutting down application container", e);
            return false;
        }

        applicationContainer = null;
        return true;
    }

    /**
     * Stops a module, meaning that components implemented by this module should not be accessed by external modules
     *
     * @param context stopping context
     * @return
     */
    public boolean stop(ApplicationContext context) {
        if (applicationContainer == null) {
            return true;
        }
        return applicationContainer.stop(context);
    }

    public void clean(ExtendedDeploymentContext context) {
        try {
            getContainerInfo().clean(context);
        } catch (Exception e) {
            context.getLogger().log(WARNING, "Exception while cleaning module '" + this + "'" + e, e);
        }
    }

    /**
     * Saves its state to the configuration. this method must be called within a transaction to the configured engine
     * instance.
     *
     * @param engine the engine configuration being persisted
     */
    public void save(Engine engine) throws TransactionFailure, PropertyVetoException {
        engine.setSniffer(getContainerInfo().getSniffer().getModuleType());
        if (appConfig != null) {
            engine.setApplicationConfig(appConfig);
        }
    }

    @Override
    public String toString() {
        return engineInfo + ", " + super.toString();
    }
}
