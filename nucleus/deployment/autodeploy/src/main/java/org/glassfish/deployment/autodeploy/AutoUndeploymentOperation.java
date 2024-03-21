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

import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Application;
import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.List;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.deployment.autodeploy.AutoDeployer.AutodeploymentStatus;
import org.glassfish.deployment.common.DeploymentProperties;
import jakarta.inject.Inject;
import jakarta.inject.Named;


import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * Performs a single auto-undeploy operation for a single file.
 * <p>
 * Note - Use the newInstance static method to obtain a fully-injected operation;
 * it is safer and more convenient than using the no-arg constructor and then
 * invoking init yourself.
 *
 * @author tjquinn
 */
@Service
@PerLookup
public class AutoUndeploymentOperation extends AutoOperation {

    @Inject
    Applications apps;

    /**
     * Creates a new, injected, and initialized AutoUndeploymentOperation object.
     *
     * @param habitat
     * @param appFile
     * @param name
     * @param target
     * @return the AutoUndeploymentOperation object
     */
    static AutoUndeploymentOperation newInstance(
            ServiceLocator habitat,
            File appFile,
            String name,
            String target) {
        AutoUndeploymentOperation o =
                (AutoUndeploymentOperation) habitat.getService(AutoUndeploymentOperation.class);

        o.init(appFile, name, target);
        return o;
    }

    private static final String COMMAND_NAME = "undeploy";

    @Inject
    private AutodeployRetryManager retryManager;

    @Inject @Named(COMMAND_NAME)
    private AdminCommand undeployCommand;

    @LogMessageInfo(message = "Attempt to create file {0} failed; no further information.", level="WARNING")
    private static final String CREATE_FAILED = "NCLS-DEPLOYMENT-02039";

    /**
     * Completes the intialization of the object.
     * @param appFile
     * @param name
     * @param target
     * @return the AutoUndeployOperation for convenience
     */
    protected AutoUndeploymentOperation init(
            File appFile,
            String name,
            String target) {
        super.init(
                appFile,
                prepareUndeployActionProperties(name, target),
                COMMAND_NAME,
                undeployCommand);
        return this;
    }

    private Properties prepareUndeployActionProperties(String archiveName, String target) {
        DeploymentProperties dProps = new DeploymentProperties();

        // we need to find the application registration name
        // which is not always the same as archive name
        String appName = archiveName;
        List<Application> applications = apps.getApplications();
        for (Application app : applications) {
            String defaultAppName = app.getDeployProperties().getProperty
                (DeploymentProperties.DEFAULT_APP_NAME);
            if (defaultAppName != null && defaultAppName.equals(archiveName)) {
                appName = app.getName();
            }
        }

        dProps.setName(appName);
//        dProps.setResourceAction(DeploymentProperties.RES_UNDEPLOYMENT);
//        dProps.setResourceTargetList(target);
        return (Properties)dProps;
    }


    /**
     * {@inheritDoc}
     */
    protected String getMessageString(AutodeploymentStatus ds, File file) {
        return localStrings.getLocalString(
                ds.undeploymentMessageKey,
                ds.undeploymentDefaultMessage,
                file);
    }

    /**
     * {@inheritDoc}
     */
    protected void markFiles(AutodeploymentStatus ds, File file) {
        /*
         * Before managing the marker file for the app, see if
         * the autodeployer is responsible for deleting this app
         * file and, if so, delete it.
         *
         * Normally users will delete the application file themselves.  Especially
         * in the case of directories, though, users may create the file
         * ${fileName}_undeployRequested and have the autodeployer delete the
         * file.
         * <p>
         * This avoids problems if the user-initiated deletion of a large
         * file or directory takes longer than the autodeployer cycle time.  If
         * a file has been removed from the top-level directory, the autodeployer
         * will see the updated timestamp on the directory and can only decide
         * that this is a new file - at least a newer file - to be autodeployed.
         * <p>
         * By allowing the auto-deployer to manage the deletion of the file the
         * user can avoid this whole scenario and, thereby, avoid accidental
         * attempts to deploy an application that the user wants gone.
         *
         */
        if (undeployedByRequestFile(file)) {
            cleanupAppAndRequest(file);
        }

        if (ds.status) {
            markUndeployed(file);
            retryManager.recordSuccessfulUndeployment(file);
        } else {
            markUndeployFailed(file);
            retryManager.recordFailedUndeployment(file);
        }
    }

    private void markUndeployed(File f) {
        try {
            deleteAllMarks(f);
            final File undeployedFile = getUndeployedFile(f);
            if ( ! undeployedFile.createNewFile()) {
                deplLogger.log(Level.WARNING,
                               CREATE_FAILED,
                               undeployedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            //ignore
        }
    }

    private void markUndeployFailed(File f) {
        try {
            deleteAllMarks(f);
            final File undeployFailedFile = getUndeployFailedFile(f);
            if ( ! undeployFailedFile.createNewFile()) {
                deplLogger.log(Level.WARNING,
                               CREATE_FAILED,
                               undeployFailedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            //ignore
        }
    }

    private boolean undeployedByRequestFile(File f) {
        return f instanceof AutoDeployedFilesManager.UndeployRequestedFile;
    }

    private void cleanupAppAndRequest(File f) {
        boolean logFine = deplLogger.isLoggable(Level.FINE);

        /*
         * Clean up the application file or directory.
         */
        if (f.isDirectory()) {
            if (logFine) {
                deplLogger.fine("Deleting autodeployed directory " + f.getAbsolutePath() + " by request");
            }
            FileUtils.liquidate(f);
        } else {
            if (logFine) {
                deplLogger.fine("Deleting autodeployed file " + f.getAbsolutePath() + " by request");
            }
            FileUtils.deleteFile(f);
        }

        /*
         * Remove the undeploy request file.
         */
        File requestFile = AutoDeployedFilesManager.appToUndeployRequestFile(f);
        if (logFine) {
            deplLogger.fine("Deleting autodeploy request file " + requestFile.getAbsolutePath());
        }
        FileUtils.deleteFile(requestFile);
    }


}
