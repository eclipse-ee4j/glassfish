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

package org.glassfish.api.deployment;

import org.glassfish.api.container.Container;

/**
 * A deployer is capable of deploying one type of applications.
 *
 * Deployers should use the ArchiveHandler to get a ClassLoader capable of loading classes and resources from the
 * archive type that is being deployed.
 *
 * In all cases the ApplicationContainer subclass must return the class loader associated with the application. In case
 * the application is deployed to more than one container the class loader can be shared and therefore should be
 * retrieved from the ArchiveHandler
 *
 * @param <T> is the container type associated with this deployer
 * @param <U> is the ApplicationContainer implementation for this deployer
 * @author Jerome Dochez
 */
public interface Deployer<T extends Container, U extends ApplicationContainer> {

    /**
     * Returns the meta data associated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    MetaData getMetaData();

    /**
     * Loads the meta date associated with the application.
     *
     * @param type type of meta-data that this deployer has declared providing.
     * @return the meta-data of type V
     */
    <V> V loadMetaData(Class<V> type, DeploymentContext context);

    /**
     * Prepares the application bits for running in the application server. For certain cases, this is generating non
     * portable artifacts and other application specific tasks. Failure to prepare should throw an exception which will
     * cause the overall deployment to fail.
     *
     * @param context of the deployment
     * @return true if the prepare phase executed successfully
     */
    boolean prepare(DeploymentContext context);

    /**
     * Loads a previously prepared application in its execution environment and return a ContractProvider instance that will
     * identify this environment in future communications with the application's container runtime.
     *
     * @param container in which the application will reside
     * @param context of the deployment
     * @return an ApplicationContainer instance identifying the running application
     */
    U load(T container, DeploymentContext context);

    /**
     * Unload or stop a previously running application identified with the ContractProvider instance. The container will be
     * stop upon return from this method.
     *
     * @param appContainer instance to be stopped
     * @param context of the undeployment
     */
    void unload(U appContainer, DeploymentContext context);

    /**
     * Clean any files and artifacts that were created during the execution of the prepare method.
     *
     * @param context deployment context
     */
    void clean(DeploymentContext context);
}
