/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.osgi.cli.remote.impl;

import org.apache.felix.shell.ShellService;
import org.glassfish.api.ActionReport;

/**
 * Service using {@link ShellService} and NOT supporting sessions.
 *
 * @author David Matejcek
 */
class FelixOsgiShellService extends OsgiShellService {

    private final ShellService service;

    FelixOsgiShellService(Object service, ActionReport report) {
        super(report);
        this.service = (ShellService) service;
    }


    @Override
    protected void execCommand(String cmdName, String cmd) throws Exception {
        if (ASADMIN_OSGI_SHELL.equals(cmdName)) {
            stdout.println("felix");
        } else {
            service.executeCommand(cmd, stdout, stderr);
        }
    }
}
