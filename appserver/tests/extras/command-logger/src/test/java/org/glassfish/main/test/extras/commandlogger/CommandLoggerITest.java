/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 */
package org.glassfish.main.test.extras.commandlogger;

import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.util.List;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.CollectLogFiles;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author Ondro Mihalyi
 */
public class CommandLoggerITest {

    private static final Asadmin ASADMIN = getAsadmin();

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true,
            value = {
                "LOG_MODE               , LOG_WRITE , LOG_READ , LOG_INTERNAL",
                "WRITE_COMMANDS         , true      , false    , false       ",
                "READ_WRITE_COMMANDS    , true      , true     , false       ",
                "INTERNAL_COMMANDS      , true      , false    , true        ",
                "ALL_COMMANDS           , true      , true     , true        ",
                "NO_COMMAND             , false     , false    , false       ",
                "                       , false     , false    , false       ",}
    )
    public void testLogWriteCommands(String logMode, boolean logWriteOp, boolean logReadOp, boolean logInternalOp) throws IOException {
        if (logMode != null) {
            assertThat(ASADMIN.exec("create-system-properties", "--target=server", "glassfish.commandlogger.logmode=" + logMode), asadminOK());
        } else {
            ASADMIN.exec("delete-system-property", "--target=server", "glassfish.commandlogger.logmode");
        }
        clearLogFile();

        // execute some write command, it doesn't have to complete successfully
        ASADMIN.exec("delete-system-property", "X");
        // execute some read command
        ASADMIN.exec("list-applications", "--long");
        // exxecute some internal command
        ASADMIN.exec("__locations");

        final List<String> lines = new CollectLogFiles()
                .collect()
                .getServerLogLines();

        if (logWriteOp) {
            assertCommandLogged(lines, "admin", "delete-system-property X");
        } else {
            assertCommandNotLogged(lines, "delete-system-property");
        }

        if (logReadOp) {
            assertCommandLogged(lines, "admin", "list-applications --long");
        } else {
            assertCommandNotLogged(lines, "list-applications");
        }

        if (logInternalOp) {
            assertCommandLogged(lines, "admin", "__locations");
        } else {
            assertCommandNotLogged(lines, "__locations");
        }

    }

    private void clearLogFile() {
        assertThat(ASADMIN.exec("rotate-log", "--target=server"), asadminOK());
    }

    private void assertCommandNotLogged(final List<String> lines, String command) {
        assertThat("log", lines, everyItem(not(containsString(command))));
    }

    private void assertCommandLogged(final List<String> lines, String user, String fullCommand) {
        assertThat("server.log", lines, hasItem(allOf(containsString(user),
                containsString(fullCommand)
        )
        ));
    }

}
