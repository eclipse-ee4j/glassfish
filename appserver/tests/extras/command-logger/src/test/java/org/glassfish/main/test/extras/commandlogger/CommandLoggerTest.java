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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Ondro Mihalyi
 */
public class CommandLoggerTest {

    private static final Asadmin ASADMIN = getAsadmin();

    @Test
    public void testLogWriteCommands() throws IOException {
        assertThat(ASADMIN.exec("create-system-properties", "--target=server", "glassfish.commandlogger.logmode=WRITE_COMMANDS"), asadminOK());
        ASADMIN.exec("delete-system-property", "X");
        AsadminResult result = ASADMIN.exec("collect-log-files");
        assertThat(result, asadminOK());
        String path = StringUtils.substringBetween(result.getStdOut(), "Created Zip file under ", ".\n");
        assertNotNull(path, () -> "zip file path parsed from " + result.getStdOut());
        File file = new File(path);
        assertThat(file.getName(), endsWith(".zip"));
        final ZipFile zipFile = new ZipFile(file);
        final ZipEntry entry = zipFile.getEntry("logs/server/server.log");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        List<String> lines = new ArrayList<>();
        while (reader.ready()) {
            lines.add(reader.readLine());
        }
        assertThat("log", lines, hasItem(containsString("delete-system-property X")));
    }

}
