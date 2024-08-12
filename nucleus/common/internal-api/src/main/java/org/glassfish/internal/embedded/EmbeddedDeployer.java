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

package org.glassfish.internal.embedded;

import java.io.File;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Contract;

/**
 * Service to deploy applications to the embedded server.
 *
 * @author Jerome Dochez
 */
@Contract
public interface EmbeddedDeployer {


    // todo : is this still used ?

    /**
     * Returns the location of the applications directory, where deployed applications
     * are saved.
     *
     * @return the deployed application directory.
     */
    public File getApplicationsDir();

    /**
     * Returns the location of the auto-deploy directory.
     *
     * @return the auto-deploy directory
     *
     */
    public File getAutoDeployDir();

    /**
     * Enables or disables the auto-deployment feature
     *
     * @param flag set to true to enable, false to disable
     */
    public void setAutoDeploy(boolean flag);

    /**
     * Deploys a file or directory to the servers passing the deployment command parameters
     * Starts the server if it is not started yet.
     *
     * @param archive archive or directory of the application
     * @param params deployment command parameters
     * @return the deployed application name
     */
    public String deploy(File archive, DeployCommandParameters params);

    /**
     * Deploys an archive abstraction to the servers passing the deployment command parameters
     *
     * @param archive archive or directory of the application
     * @param params deployment command parameters
     * @return the deployed application name
     */
    public String deploy(ReadableArchive archive, DeployCommandParameters params);


    // todo : add redeploy ?

    /**
     * Undeploys a previously deployed application
     *
     * @param name name returned by {@link EmbeddedDeployer#deploy(File, org.glassfish.api.deployment.DeployCommandParameters}
     * @param params the undeployment parameters, can be null for default values
     */
    public void undeploy(String name, UndeployCommandParameters params);

    /**
     * Undeploys all deployed applications.
     */
    public void undeployAll();
}
