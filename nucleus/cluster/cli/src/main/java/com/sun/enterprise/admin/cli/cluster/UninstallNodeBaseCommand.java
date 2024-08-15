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

import jakarta.inject.Inject;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Rajiv Mordani
 * @author Byron Nevins
 */
@Service
@PerLookup
abstract class UninstallNodeBaseCommand extends NativeRemoteCommandsBase {
    @Param(name = "installdir", optional = true, defaultValue = "${com.sun.aas.productRoot}")
    private String installDir;
    @Param(optional = true, defaultValue = "false")
    private boolean force;
    @Inject
    private ServiceLocator habitat;

    abstract void deleteFromHosts() throws CommandException;

    @Override
    protected void validate() throws CommandException {
        super.validate();
        Globals.setDefaultHabitat(habitat);
        installDir = resolver.resolve(installDir);
    }

    @Override
    protected final int executeCommand() throws CommandException {
        deleteFromHosts();
        return SUCCESS;
    }

    final String getInstallDir() {
        return installDir;
    }

    final boolean getForce() {
        return force;
    }
}
