/*
 * Copyright (c) 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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
package org.glassfish.main.admin.test;

import java.util.stream.Stream;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.StartServ;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

/**
 * @author Ondro Mihalyi
 */
public class StartServITest {

    private static final StartServ STARTSERV = GlassFishTestEnvironment.getStartServ();
    private static final StartServ STARTSERV_IN_TOPLEVEL_BIN = GlassFishTestEnvironment.getStartServInTopLevelBin();
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();
    private static final String STARTSERV_DOMAIN_NAME = "startserv-domain";
    private static final String NOT_EXISTING_DOMAIN_NAME = "not-existing-domain";

    @BeforeAll
    static void setupDomain() {
        AsadminResult result = ASADMIN.exec("list-domains");
        if (!result.getOutput().contains(STARTSERV_DOMAIN_NAME)) {
            ASADMIN.exec("create-domain", "--nopassword", STARTSERV_DOMAIN_NAME);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(StartServArgumentsProvider.class)
    public void startServerInForeground(StartServ startServ) {
        try {
            AsadminResult result = startServ.withTextToWaitFor("Total startup time including CLI").exec(STARTSERV_DOMAIN_NAME);
            assertThat(result, asadminOK());
        } finally {
            ASADMIN.exec("stop-domain", STARTSERV_DOMAIN_NAME);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(StartServArgumentsProvider.class)
    @DisabledOnOs(value = WINDOWS, disabledReason = "startserv.bat is just trivial and doesn't give the error output")
    public void reportCorrectErrorIfAlreadyRunning(StartServ startServ) {
        try {
            AsadminResult result = startServ.withTextToWaitFor("Total startup time including CLI").exec(STARTSERV_DOMAIN_NAME);
            assertThat(result, asadminOK());
            result = startServ.withNoTextToWaitFor().exec(STARTSERV_DOMAIN_NAME);
            assertThat(result.getStdErr(), containsString("There is a process already using the admin port"));
        } finally {
            ASADMIN.exec("stop-domain", STARTSERV_DOMAIN_NAME);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(StartServArgumentsProvider.class)
    public void reportCorrectErrorIfInvalidCommand(StartServ startServ) {
        AsadminResult result = startServ.withNoTextToWaitFor().exec(NOT_EXISTING_DOMAIN_NAME);
        assertThat(result.getStdErr(), containsString("There is no such domain directory"));
    }

    @AfterAll
    static void deleteDomain() {
        AsadminResult result = ASADMIN.exec("list-domains");
        if (result.getOutput().contains(STARTSERV_DOMAIN_NAME)) {
            ASADMIN.exec("delete-domain", STARTSERV_DOMAIN_NAME);
        }
    }

    static class StartServArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    // Will be as [1]
                    Arguments.of(STARTSERV),
                    // Will be as [2]
                    Arguments.of(STARTSERV_IN_TOPLEVEL_BIN)
            );
        }
    }
}
