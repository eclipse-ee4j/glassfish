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
package org.glassfish.main.itest.tools.asadmin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;

import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Ondro Mihalyi
 */
public class CollectLogFiles {

    private AsadminResult result;

    private File fileWithLogs;

    public CollectLogFiles collect() throws IOException {
        result = getAsadmin().exec("collect-log-files");
        assertThat(result, asadminOK());
        String path = StringUtils.substringBetween(result.getStdOut(), "Created Zip file under ", ".\n");
        assertThat("zip file path parsed from " + result.getStdOut(), path, notNullValue());
        fileWithLogs = new File(path);
        fileWithLogs.deleteOnExit();
        assertThat(fileWithLogs.getName(), endsWith(".zip"));
        return this;
    }

    public List<String> getServerLogLines() throws IOException {
        return getLogLines("logs/server/server.log");
    }

    private List<String> getLogLines(String logFilePath) throws IOException {
        final ZipFile zipFile = new ZipFile(fileWithLogs);
        final ZipEntry entry = zipFile.getEntry(logFilePath);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        List<String> lines = new ArrayList<>();
        while (reader.ready()) {
            lines.add(reader.readLine());
        }
        return lines;
    }

}
