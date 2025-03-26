/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.deployment.common;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.OpsParams;

/**
 * Convenient superclass for Deployers which only do prepare and clean up and do not actually load/unload application
 *
 */
public abstract class SimpleDeployer<T extends Container, U extends ApplicationContainer<?>> implements Deployer<T, U> {

    /**
     * Loads the meta date associated with the application.
     *
     * @parameters type type of metadata that this deployer has declared providing.
     */
    @Override
    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    /**
     * Prepares the application bits for running in the application server. For certain cases, this is generating non
     * portable artifacts and other application specific tasks. Failure to prepare should throw an exception which will
     * cause the overall deployment to fail.
     *
     * @param dc deployment context
     * @return true if the prepare phase was successful
     *
     */
    @Override
    public boolean prepare(DeploymentContext dc) {
        try {
            if (!dc.getCommandParameters(OpsParams.class).origin.isArtifactsPresent()) {
                // only generate artifacts when no artifacts are present
                generateArtifacts(dc);
            }
            return true;
        } catch (Exception ex) {
            // re-throw all the exceptions as runtime exceptions
            RuntimeException re = new RuntimeException(ex.getMessage());
            re.initCause(ex);
            throw re;
        }
    }

    /**
     * No-op
     */
    @Override
    public U load(T container, DeploymentContext context) {
        return null;
    }

    /**
     * No-op
     */
    @Override
    public void unload(U appContainer, DeploymentContext context) {
    }

    /**
     * Clean any files and artifacts that were created during the execution of the prepare method.
     *
     * @param context deployment context
     */
    @Override
    public void clean(DeploymentContext context) {
        try {
            if (context.getCommandParameters(OpsParams.class).origin.needsCleanArtifacts()) {
                // only clean artifacts when needed
                cleanArtifacts(context);
            }
        } catch (Exception ex) {
            // re-throw all the exceptions as runtime exceptions
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    @Override
    public MetaData getMetaData() {
        return new MetaData(false, null, null);
    }

    // methods for implementation clsses to override

    /**
     * No-op
     */
    protected void generateArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    /**
     * No-op
     */
    protected void cleanArtifacts(DeploymentContext dc) throws DeploymentException {

    }

}
