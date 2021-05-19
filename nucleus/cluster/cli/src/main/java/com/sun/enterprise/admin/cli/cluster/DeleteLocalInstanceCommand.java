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

import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import com.sun.enterprise.util.StringUtils;
import java.io.*;


import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;


/**
 * Delete a local server instance.
 * Wipeout the node dir if it is the last instance under the node
 *
 * Performance Note:  getServerDirs().getServerDir() is all inlined by the JVM
 * because the class instance is immutable.
 *
 * @author Byron Nevins
 */
@Service(name = "delete-local-instance")
@PerLookup
public class DeleteLocalInstanceCommand extends LocalInstanceCommand {

    @Param(name = "instance_name", primary = true, optional = true)
    private String instanceName0;

    /** initInstance goes to great lengths to figure out the correct directory structure.
     * We don't care about such errors.  If the dir is not there -- then this is a
     * simple error about trying to delete an instance that doesn't exist...
     * Thank goodness for overriding methods!!
     * @throws CommandException
     */
    @Override
    protected void initInstance() throws CommandException {
        try {
            super.initInstance();
        }
        catch (CommandException e) {
            throw e;
        }
        catch (Exception e) {
            throw new CommandException(Strings.get("DeleteInstance.noInstance"));
        }
    }

    /**
     * We most definitely do not want to create directories for nodes here!!
     * @param f the directory to create
     */
    @Override
    protected boolean mkdirs(File f) {
        return false;
    }

    @Override
    protected void validate()
            throws CommandException, CommandValidationException {
        instanceName = instanceName0;
        super.validate();
        if (!StringUtils.ok(getServerDirs().getServerName()))
            throw new CommandException(Strings.get("DeleteInstance.noInstanceName"));

        File dasProperties = getServerDirs().getDasPropertiesFile();

        if (dasProperties.isFile()) {
            setDasDefaults(dasProperties);
        }

        if (!getServerDirs().getServerDir().isDirectory())
            throw new CommandException(Strings.get("DeleteInstance.noWhack",
                    getServerDirs().getServerDir()));
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        if (isRunning()) {
            throw new CommandException(Strings.get("DeleteInstance.running"));
        }

        doRemote();
        whackFilesystem();
        return SUCCESS;
    }

    /**
     * Ask DAS to wipe it out from domain.xml
     * If DAS isn't running -- ERROR -- return right away with a thrown Exception
     * If DAS is running, and instance not registered on DAS, do not unregister instance -- OK
     * If DAS is running, and instance is registered on DAS, then unregister instance -- OK
     *      - If _unregister-instance is successful - OK
     *      - If _unregister-instance fails - ERROR - Exception thrown
     */
    private void doRemote() throws CommandException {
        if (!isDASRunning()) {
            String newString = Strings.get("DeleteInstance.remoteError",
                    programOpts.getHost(), "" + programOpts.getPort());
            throw new CommandException(newString);
        }

        if (isRegisteredToDas()) {
            RemoteCLICommand rc = new RemoteCLICommand("_unregister-instance", programOpts, env);
            rc.execute("_unregister-instance", getServerDirs().getServerName());
        }
    }

    private boolean isDASRunning() {
        try {
            getUptime();
            return true;
        } catch (CommandException ex) {
            return false;
        }
    }

    /**
     * If the instance is not registered on DAS (server xml entry doesn't exist
     * in domain.xml), the get command will throw a CommandException
     */
    private boolean isRegisteredToDas() {
        boolean isRegistered;
        RemoteCLICommand rc;
        String INSTANCE_DOTTED_NAME = "servers.server." + instanceName;
        try {
            rc = new RemoteCLICommand("get", this.programOpts, this.env);
            rc.executeAndReturnOutput("get", INSTANCE_DOTTED_NAME);
            isRegistered = true;
        } catch (CommandException ce) {
            isRegistered = false;
        }
        return isRegistered;
    }
}
