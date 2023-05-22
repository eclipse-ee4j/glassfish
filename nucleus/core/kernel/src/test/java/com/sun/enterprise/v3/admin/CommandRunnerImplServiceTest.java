/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.admin;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.InternalSystemAdministrator;
import org.glassfish.main.core.kernel.test.KernelJUnitExtension;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test the command runner implementation: {@link CommandRunnerImpl}
 *
 * @author Jerome Dochez
 * @author David Matejcek
 */
@ExtendWith(KernelJUnitExtension.class)
public class CommandRunnerImplServiceTest {

    @Inject
    private ServiceLocator locator;

    @Inject
    private MockGenerator mockGenerator;

    private CommandRunnerImpl commandRunner;
    private InternalSystemAdministrator sysAdmin;


    @BeforeEach
    public void init() {
        commandRunner = locator.getService(CommandRunnerImpl.class);
        sysAdmin = locator.getService(InternalSystemAdministrator.class);
    }


    @Test
    public void nonExistingCommand() {
        assertNotNull(commandRunner);
        assertNotNull(sysAdmin.getSubject());
        assertThat(sysAdmin.getSubject().getPrivateCredentials(), hasSize(1));

        ActionReport report = commandRunner.getActionReport("plain");
        CommandInvocation inv = commandRunner.getCommandInvocation("doesnt-exist", report, sysAdmin.getSubject());
        inv.execute();

        assertAll(
            () -> assertNull(report.getFailureCause()),
            () -> assertEquals(ExitCode.FAILURE, report.getActionExitCode()),
            () -> assertEquals("text/plain", report.getContentType()),
            () -> assertEquals(
                "Command doesnt-exist not found. \n"
                + "Check the entry of command name. This command may be provided by a package that is not installed.",
                report.getMessage()),
            () -> assertNotNull(report.getTopMessagePart()),
            () -> assertThat(report.getTopMessagePart().getChildren(), hasSize(0))
        );
    }
}
