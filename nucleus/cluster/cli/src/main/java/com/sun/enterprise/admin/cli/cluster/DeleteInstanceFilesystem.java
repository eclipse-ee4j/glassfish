/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.StringUtils;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;


// TODO TODO
// wipe out the tree if this is the last instance
// TODO TODO TODO
/**
 * Delete a local server instance.
 */
@Service(name = "_delete-instance-filesystem")
@PerLookup
public class DeleteInstanceFilesystem extends LocalInstanceCommand {

    @Param(name = "instance_name", primary = true, optional = false)
    private String instanceName0;

    @Override
    final protected void validate()
            throws CommandException, CommandValidationException {
        instanceName = instanceName0;
        super.validate();

        if (!StringUtils.ok(getServerDirs().getServerName())) {
            throw new CommandException(Strings.get("DeleteInstance.noInstanceName"));
        }
    }


    @Override
    protected final int executeCommand() throws CommandException {

        if (ProcessUtils.isAlive(getServerDirs().getPidFile())) {
            throw new CommandException(Strings.get("DeleteInstance.running"));
        }

        whackFilesystem();

        return SUCCESS;
    }
}
