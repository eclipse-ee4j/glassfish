/*
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

package org.glassfish.deployment.autodeploy;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.deployment.autodeploy.AutoDeployer.AutodeploymentStatus;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.jvnet.hk2.annotations.Service;

/**
 * Performs a single auto-deployment operation for a single file.
 * <p>
 * Note - Use the newInstance static method to obtain a fully-injected operation;
 * it is safer and more convenient than using the no-arg constructor and then
 * invoking init yourself.
 *
 * @author tjquinn
 */
@Service
@PerLookup
public class AutoDeploymentOperation extends AutoOperation {

    /**
     * Creates a fully-injected, ready-to-use AutoDeploymentOperation object.
     * @param habitat
     * @param renameOnSuccess
     * @param file
     * @param enabled
     * @param virtualServer
     * @param forceDeploy
     * @param verify
     * @param preJspCompilation
     * @param target
     * @return the injected, initialized AutoDeploymentOperation
     */
    static AutoDeploymentOperation newInstance(
            ServiceLocator habitat,
            boolean renameOnSuccess,
            File file,
            boolean enabled,
            String virtualServer,
            boolean forceDeploy,
            boolean verify,
            boolean preJspCompilation,
            String target) {

        AutoDeploymentOperation o =
                (AutoDeploymentOperation) habitat.getService(AutoDeploymentOperation.class);

        o.init(renameOnSuccess, file, enabled, virtualServer, forceDeploy, verify, preJspCompilation, target);
        return o;
    }

    private boolean renameOnSuccess;

    private static final String COMMAND_NAME = "deploy";

    @Inject @Named(COMMAND_NAME)
    private AdminCommand deployCommand;

    @Inject
    private AutodeployRetryManager retryManager;

    public static final Logger deplLogger =
        org.glassfish.deployment.autodeploy.AutoDeployer.deplLogger;

    @LogMessageInfo(message = "Attempt to create file {0} failed; no further information.", level="WARNING")
    private static final String CREATE_FILE_FAILED = "NCLS-DEPLOYMENT-02034";

    /**
     * Completes the initialization of the object.
     * @param renameOnSuccess
     * @param file
     * @param enabled
     * @param virtualServer
     * @param forceDeploy
     * @param verify
     * @param preJspCompilation
     * @param target
     * @return the object itself for convenience
     */
    protected AutoDeploymentOperation init (
            boolean renameOnSuccess,
            File file,
            boolean enabled,
            String virtualServer,
            boolean forceDeploy,
            boolean verify,
            boolean preJspCompilation,
            String target) {

        super.init(file, getDeployActionProperties(
                        file,
                        enabled,
                        virtualServer,
                        forceDeploy,
                        verify,
                        preJspCompilation,
                        target),
                        COMMAND_NAME,
                        deployCommand);
        this.renameOnSuccess = renameOnSuccess;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    protected String getMessageString(AutodeploymentStatus ds, File file) {
        return localStrings.getLocalString(
                ds.deploymentMessageKey,
                ds.deploymentDefaultMessage,
                file);
    }

    /**
     * {@inheritDoc}
     */
    protected void markFiles(AutodeploymentStatus ds, File file) {
        /*
         * One reason an auto-deployment may fail is if the auto-deployed
         * file is a directory and the directory's contents were not yet
         * complete when the autodeployer detected a change in the top-level
         * directory file's timestamp.  Retry a failed autodeploy of a
         * directory for the prescribed retry period or until it succeeds.
         *
         * Similarly, a JAR file - or any legitimate application file - might
         * be copied over a period of time, so an initial attempt to deploy it
         * might fail because the file is incomplete but a later attempt might
         * work.
         */
        if (ds != AutodeploymentStatus.SUCCESS && ds != AutodeploymentStatus.WARNING) {
            try {
                retryManager.recordFailedDeployment(file);
            } catch (AutoDeploymentException ex) {
                /*
                 * The retry manager has concluded that this most recent
                 * failure should be the last one.
                 */
                markDeployFailed(file);
                retryManager.endMonitoring(file);
            }
        } else {
            retryManager.recordSuccessfulDeployment(file);
            if (ds.status) {
                if (renameOnSuccess) {
                    markDeployed(file);
                }
            } else {
                markDeployFailed(file);
            }
        }
    }

    // Methods for creating operation status file(s)
    private void markDeployed(File f) {
        try {
            deleteAllMarks(f);
            final File deployedFile = getDeployedFile(f);
            if ( ! deployedFile.createNewFile()) {
                deplLogger.log(Level.WARNING,
                               CREATE_FILE_FAILED,
                               deployedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            //ignore
        }
    }

    private void markDeployFailed(File f) {
        try {
            deleteAllMarks(f);
            final File deployFailedFile = getDeployFailedFile(f);
            if ( ! deployFailedFile.createNewFile()) {
                deplLogger.log(Level.WARNING,
                               CREATE_FILE_FAILED,
                               deployFailedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            //ignore
        }
    }

    private static Properties getDeployActionProperties(
            File deployablefile,
            boolean enabled,
            String virtualServer,
            boolean forceDeploy,
            boolean verify,
            boolean jspPreCompilation,
            String target){

        DeploymentProperties dProps = new DeploymentProperties();
        dProps.setPath(deployablefile.getAbsolutePath());
//        dProps.setUpload(false);
        dProps.setEnabled(enabled);
        if (virtualServer != null) {
            dProps.setVirtualServers(virtualServer);
        }
        dProps.setForce(forceDeploy);
        dProps.setVerify(verify);
        dProps.setPrecompileJSP(jspPreCompilation);
//        dProps.setResourceAction(DeploymentProperties.RES_DEPLOYMENT);
//        dProps.setResourceTargetList(target);

        dProps.setProperty(DeploymentProperties.LOG_REPORTED_ERRORS, "false");
        return (Properties)dProps;
    }
}
