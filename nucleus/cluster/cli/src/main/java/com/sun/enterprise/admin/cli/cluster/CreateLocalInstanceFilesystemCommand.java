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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.util.net.NetUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.CLIConstants.DEFAULT_HOSTNAME;
import static com.sun.enterprise.admin.cli.CLIConstants.K_DAS_HOST;
import static com.sun.enterprise.admin.cli.CLIConstants.K_DAS_IS_SECURE;
import static com.sun.enterprise.admin.cli.CLIConstants.K_DAS_PORT;
import static com.sun.enterprise.admin.cli.CLIConstants.K_DAS_PROTOCOL;

/**
 *  This is a local command that creates a local instance.
 *  Create the local directory structure
 *  nodes/<host-name>/
 *   || ---- agent
 *             || ---- config
 *                     | ---- das.properties
 *   || ---- <server-instance-1>
 *   || ---- <server-instance-2>
 *
 */
@Service(name = "_create-instance-filesystem")
@PerLookup
public class CreateLocalInstanceFilesystemCommand extends LocalInstanceCommand {

    @Param(name = "instance_name", primary = true)
    private String instanceName0;

    String DASHost;
    int DASPort = -1;
    String DASProtocol;
    boolean dasIsSecure;

    private File agentConfigDir = null;
    private File dasPropsFile = null;
    private Properties dasProperties;
    protected boolean setDasDefaultsOnly = false;

    /**
     */
    @Override
    protected void validate()
            throws CommandException {

        if(ok(instanceName0))
            instanceName = instanceName0;
        else
            throw new CommandException(Strings.get("Instance.badInstanceName"));

        isCreateInstanceFilesystem = true;

        super.validate();

        String agentPath = "agent" + File.separator + "config";
        agentConfigDir = new File(nodeDirChild, agentPath);
        dasPropsFile = new File(agentConfigDir, "das.properties");

        if (dasPropsFile.isFile()) {
            //Issue GLASSFISH-15263
            //Don't validate for localhost - can't tell if it's user specified or default.
            //Just use what's in das.properties so user doesn't have to specify --host
            if (programOpts.getHost() != null && !programOpts.getHost().equals(DEFAULT_HOSTNAME)) {
                //validate must come before setDasDefaults
                validateDasOptions(programOpts.getHost(), String.valueOf(programOpts.getPort()),
                        String.valueOf(programOpts.isSecure()), dasPropsFile);
            }
            setDasDefaults(dasPropsFile);
            if (!setDasDefaultsOnly) {
                String nodeDirChildName = nodeDirChild != null ? nodeDirChild.getName() : "";
                String nodeName = node != null ? node : nodeDirChildName;
                logger.info(Strings.get("Instance.existingDasPropertiesWarning",
                    programOpts.getHost(), "" + programOpts.getPort(), nodeName));
            }
        }

        DASHost = programOpts.getHost();
        DASPort = programOpts.getPort();
        dasIsSecure = programOpts.isSecure();
        DASProtocol = "http";

    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException {

        // Even though this is a local only command, we don't want to
        // bake the DAS host and port into das.properties if it does not
        // appear to be valid. So we check it. See IT bug 12943
        checkDASCoordinates();

        return createDirectories();
    }

    private int createDirectories() throws CommandException {
        boolean createDirsComplete = false;
        File badfile = null;
        while (badfile == null && !createDirsComplete) {
            if (!agentConfigDir.isDirectory()) {
                if (!agentConfigDir.mkdirs()) {
                    badfile = agentConfigDir;
                }
            }
            createDirsComplete = true;
        }
        if (badfile != null) {
            throw new CommandException(Strings.get("Instance.cannotMkDir", badfile));
        }
        writeProperties();
        return SUCCESS;
    }

    private void writeProperties() throws CommandException {
        String filename = "";
        try {
            filename = dasPropsFile.getName();
            if (!dasPropsFile.isFile()) {
                writeDasProperties();
            }
        } catch (IOException ex) {
            throw new CommandException(Strings.get("Instance.cantWriteProperties", filename), ex);
        }
    }

    private void writeDasProperties() throws IOException {
        if (dasPropsFile.createNewFile()) {
            dasProperties = new Properties();
            dasProperties.setProperty(K_DAS_HOST, DASHost);
            dasProperties.setProperty(K_DAS_PORT, String.valueOf(DASPort));
            dasProperties.setProperty(K_DAS_IS_SECURE, String.valueOf(dasIsSecure));
            dasProperties.setProperty(K_DAS_PROTOCOL, DASProtocol);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(dasPropsFile);
                dasProperties.store(fos, Strings.get("Instance.dasPropertyComment"));
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

   /**
    * Makes sure something is running at the DASHost and DASPort.
    * We intentionally do not do an operation that requires authentication
    * since we may be called in a context where authentication is not
    * provided (like over SSH).
    * This method assumes that _create_instance_filesystem is being called
    * by the DAS via SSH -- so the DAS should be running.
    *
    * @throws CommandException
    */
    private void checkDASCoordinates() throws CommandException {
        // See if hostname is known to us
        try {
            // Check if hostName is valid by looking up its address
            InetAddress.getByName(DASHost);
        } catch (UnknownHostException e) {
            String thisHost = NetUtils.getHostName();
            String msg = Strings.get("Instance.DasHostUnknown",
                    DASHost, thisHost);
            throw new CommandException(msg, e);
        }

        // See if DAS is reachable
        if (! NetUtils.isRunning(DASHost, DASPort)) {
            // DAS provided host and port
            String thisHost = NetUtils.getHostName();
            String msg = Strings.get("Instance.DasHostUnreachable",
                    DASHost, Integer.toString(DASPort), thisHost);
            throw new CommandException(msg);
        }
    }

}
