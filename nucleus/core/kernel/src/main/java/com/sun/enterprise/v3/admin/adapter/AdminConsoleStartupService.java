/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.adapter;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

@Service(name = "AdminConsoleStartupService")
@RunLevel(PostStartupRunLevel.VAL)
public class AdminConsoleStartupService {

    private static final Logger logger = KernelLoggerInfo.getLogger();

    @Inject
    @Optional
    private AdminConsoleAdapter adminConsoleAdapter;

    @Inject
    private ServerEnvironmentImpl serverEnvironment;

    @PostConstruct
    public void postConstruct() {
        // There may be no console in this environment.
        if (adminConsoleAdapter == null) {
            return;
        }

        // This service must run only on the server where the console should run. Currently, that server is DAS.
        // If and when the console becomes disassociated with DAS, this logic will need to be modified.
        if (!serverEnvironment.isDas()) {
            return;
        }

        ConsoleLoadingOption loadingOption = adminConsoleAdapter.getLoadingOption();

        logger.log(Level.FINE, "AdminConsoleStartupService: Console loading option is {0}", loadingOption);

        if (loadingOption == ConsoleLoadingOption.ALWAYS) {
            handleAlways();
        }
    }

    private void handleAlways() {
        adminConsoleAdapter.initRest();
        synchronized (this) {
            if (!adminConsoleAdapter.isInstalling() && !adminConsoleAdapter.isApplicationLoaded()) {
                adminConsoleAdapter.loadConsole();
            }
        }
    }
}
