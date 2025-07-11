/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.deployment.autodeploy.AutoDeployer.AutodeploymentStatus;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.InternalSystemAdministrator;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.jvnet.hk2.annotations.Service;

/**
 * Abstract class for operations the AutoDeployer can perform (currently
 * deploy and undeploy).
 * <p>
 * AutoOperation and its subclasses have no-arg constructors so they can be
 * initialized as services and an init method that accepts what might otherwise
 * be constructor arguments.
 *
 * @author tjquinn
 */
@Service
@PerLookup
public abstract class AutoOperation {

    public static final Logger deplLogger =
        org.glassfish.deployment.autodeploy.AutoDeployer.deplLogger;

    @LogMessageInfo(message = "{0}", level="INFO")
    private static final String INFO_MSG = "NCLS-DEPLOYMENT-02035";

    @LogMessageInfo(message = "{0}", level="WARNING")
    private static final String WARNING_MSG = "NCLS-DEPLOYMENT-02036";

    @LogMessageInfo(message = "Error occurred: ", cause="An exception was caught when the operation was attempted", action="See the exception to determine how to fix the error", level="SEVERE")
    private static final String EXCEPTION_OCCURRED = "NCLS-DEPLOYMENT-02037";

    @LogMessageInfo(message = "Attempt to delete file {0} failed; no further information.", level="WARNING")
    private static final String DELETE_FAILED = "NCLS-DEPLOYMENT-02038";

    final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(AutoDeployer.class);

    /**
     * Used in deleting all marker files for a given app.
     */
    final static private String [] autoDeployFileSuffixes = new String[] {
            AutoDeployConstants.DEPLOYED,
            AutoDeployConstants.DEPLOY_FAILED,
            AutoDeployConstants.UNDEPLOYED,
            AutoDeployConstants.UNDEPLOY_FAILED,
            AutoDeployConstants.PENDING
        };

    private File file;
    private Properties props;
    private String commandName;
    private AdminCommand command;

    @Inject
    private CommandRunner<Job> commandRunner;

    @Inject
    private AutodeployRetryManager retryManager;

    @Inject
    private InternalSystemAdministrator internalSystemAdministrator;

    /**
     * Initializes the AutoOperation.
     * @param file the File of interest
     * @param props command-line options to be passed to the relevant AdminCommand (deploy or undeploy)
     * @param commandName name of the command to execute
     * @param command the AdminCommand descendant to execute
     * @return this same operation
     */
    AutoOperation init(File file, Properties props, String commandName, AdminCommand command) {
        this.file = file;
        this.props = props;
        this.commandName = commandName;
        this.command = command;
        return this;
    }

    /**
     * Marks the files relevant to the specified file appropriately given the
     * outcome of the command as given in the status.
     * @param ds AutodeploymentStatus indicating the outcome of the operation
     * @param file file of interest
     */
    protected abstract void markFiles(AutodeploymentStatus ds, File file);

    /**
     * Returns the appropriate message string for the given operation and the
     * outcome.
     * @param ds AutodeploymentStatus value giving the outcome of the operation
     * @param file file of interest
     * @return message string to be logged
     */
    protected abstract String getMessageString(AutodeploymentStatus ds, File file);

    /**
     * Executes the operation
     * @return true/false depending on the outcome of the operation
     * @throws org.glassfish.deployment.autodeploy.AutoDeploymentException
     */
    final AutodeploymentStatus run() throws AutoDeploymentException {
        try {
            ParameterMap p = new ParameterMap();
            for (Map.Entry<Object,Object> entry : props.entrySet()) {
                p.set((String)entry.getKey(), (String)entry.getValue());
            }
            ActionReport report = commandRunner.getActionReport("hk2-agent");
            CommandInvocation<Job> inv = commandRunner.getCommandInvocation(commandName, report,
                internalSystemAdministrator.getSubject());
            inv.parameters(p).execute(command);
            AutodeploymentStatus ds = AutodeploymentStatus.forExitCode(report.getActionExitCode());
            if (ds.status) {
                deplLogger.log(Level.INFO, INFO_MSG, getMessageString(ds, file));
            } else {
                if (report.getMessage() == null) {
                    deplLogger.log(Level.WARNING, WARNING_MSG, getMessageString(ds, file));
                } else {
                    deplLogger.log(Level.WARNING, WARNING_MSG, report.getMessage());
                }
            }
            markFiles(ds, file);
            /*
             * Choose the final status to report, based on the outcome of the
             * deployment as well as whether we are now monitoring this file.
             */
            ds = retryManager.chooseAutodeploymentStatus(report.getActionExitCode(), file);
            return ds;
        } catch (Exception ex) {
            /*
             * Log and continue.
             */
            deplLogger.log(Level.SEVERE, EXCEPTION_OCCURRED, ex);
            return AutodeploymentStatus.FAILURE;
        }
    }

    private File getSuffixedFile(File f, String suffix) {
        String absPath = f.getAbsolutePath();
        return new File(absPath + suffix);
    }

    /**
     * Returns a File object for the "deployed" marker file for a given file.
     * @param f
     * @return File for the "deployed" marker file
     */
    protected File getDeployedFile(File f) {
        return getSuffixedFile(f, AutoDeployConstants.DEPLOYED);
    }

    /**
     * Returns a File object for the "deploy failed" marker file for a given file.
     * @param f
     * @return File for the "deploy failed" marker file
     */
    protected File getDeployFailedFile(File f) {
        return getSuffixedFile(f, AutoDeployConstants.DEPLOY_FAILED);
    }

    /**
     * Returns a File object for the "undeployed" marker file for a given file.
     * @param f
     * @return File for the "undeployed" marker file
     */
    protected File getUndeployedFile(File f) {
        return getSuffixedFile(f, AutoDeployConstants.UNDEPLOYED);
    }

    /**
     * Returns a File object for the "undeploy failed" marker file for a given file.
     * @param f
     * @return File for the "undeploy failed" marker file
     */
    protected File getUndeployFailedFile(File f) {
        return getSuffixedFile(f, AutoDeployConstants.UNDEPLOY_FAILED);
    }


    /**
     * Deletes all possible marker files for the file.
     * @param f the File whose markers should be removed
     */
    protected void deleteAllMarks(File f) {
        try {
            for (String suffix : autoDeployFileSuffixes) {
                final File suffixedFile = getSuffixedFile(f, suffix);
                if (suffixedFile.exists()) {
                    if (!suffixedFile.delete()) {
                        deplLogger.log(Level.WARNING, DELETE_FAILED, suffixedFile.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

}
