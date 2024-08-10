/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.internal.data;

import java.util.logging.Logger;

import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;

import static java.util.logging.Level.FINE;

/**
 * This class holds information about a particular container such as a reference to the sniffer, the container itself
 * and the list of applications deployed in that container.
 *
 * @author Jerome Dochez
 */
public class EngineInfo<T extends Container, U extends ApplicationContainer<?>> {

    final ServiceHandle<T> container;
    final Sniffer sniffer;
    ContainerRegistry registry;
    Deployer<T, U> deployer;

    /**
     * Creates a new ContractProvider info with references to the container, the sniffer and the connector module
     * implementing the ContractProvider/Deployer interfaces.
     *
     * @param container instance of the container
     * @param sniffer sniffer associated with that container
     */
    public EngineInfo(ServiceHandle<T> container, Sniffer sniffer, ClassLoader cloader) {
        this.container = container;
        this.sniffer = sniffer;
    }

    /**
     * Returns the container instance
     *
     * @return the container instance
     */
    public T getContainer() {
        return container.getService();
    }

    /**
     * Returns the sniffer associated with this container
     *
     * @return the sniffer instance
     */
    public Sniffer getSniffer() {
        return sniffer;
    }

    /**
     * Returns the deployer instance for this container
     *
     * @return Deployer instance
     */
    public Deployer<T, U> getDeployer() {
        return deployer;
    }

    /**
     * Sets the deployer associated with this container
     *
     * @param deployer
     */
    public void setDeployer(Deployer<T, U> deployer) {
        this.deployer = deployer;
    }

    public void load(ExtendedDeploymentContext context) {
    }

    public void unload(ExtendedDeploymentContext context) throws Exception {
    }

    public void clean(ExtendedDeploymentContext context) throws Exception {
        getDeployer().clean(context);
    }

    /*
     * Sets the registry this container belongs to
     *
     * @param the registry owning me
     */
    public void setRegistry(ContainerRegistry registry) {
        this.registry = registry;
    }

    // Todo : take care of Deployer when unloading...
    public void stop(Logger logger) {
        if (getDeployer() != null) {
            ServiceHandle<?> serviceHandle = registry.serviceLocator.getServiceHandle(getDeployer().getClass());
            if (serviceHandle != null) {
                serviceHandle.close();
            }
        }

        if (container != null && container.isActive()) {
            container.close();
        }

        registry.removeContainer(this);

        if (logger.isLoggable(FINE)) {
            logger.fine("Container " + getContainer().getName() + " stopped");
        }
    }

    @Override
    public String toString() {
        return deployer + ", " + super.toString();
    }
}
