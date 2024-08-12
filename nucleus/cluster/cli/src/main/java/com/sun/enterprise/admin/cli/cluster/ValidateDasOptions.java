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

import java.io.File;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Validate DAS host, port, secure options.
 */
@Service(name = "_validate-das-options")
@PerLookup
public class ValidateDasOptions extends LocalInstanceCommand {

    @Param(name = "instance_name", primary = true, optional = false)
    private String instanceName0;

    @Override
    protected boolean mkdirs(File f) {
        return false;
    }

    @Override
    protected boolean isDirectory(File f) {
        return true;
    }

    @Override
    protected boolean setServerDirs() {
        return false;
    }

    @Override
    final protected void validate() throws CommandException {
        instanceName = instanceName0;
        super.validate();

        File dasProperties = new File(new File(nodeDirChild, "agent"
                + File.separator + "config"), "das.properties");

        if (dasProperties.isFile()) {
            validateDasOptions(programOpts.getHost(), String.valueOf(programOpts.getPort()),
                    String.valueOf(programOpts.isSecure()), dasProperties);
        }
    }

    /**
     */
    @Override
    final protected int executeCommand() throws CommandException {
        return SUCCESS;
    }
}
