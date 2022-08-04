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

package org.glassfish.main.admin.test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.main.admin.test.tool.asadmin.Asadmin;
import org.glassfish.main.admin.test.tool.asadmin.AsadminResult;
import org.glassfish.main.admin.test.tool.asadmin.GlassFishTestEnvironment;
import org.glassfish.tests.utils.junit.matcher.TextFileMatchers;
import org.hamcrest.io.FileMatchers;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.StringUtils.replaceChars;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.glassfish.main.admin.test.tool.AsadminResultMatcher.asadminOK;
import static org.glassfish.main.admin.test.tool.asadmin.GlassFishTestEnvironment.getDomain1Directory;
import static org.glassfish.tests.utils.junit.matcher.TextFileMatchers.getterMatches;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author David Matejcek
 */
public class AsadminLoggingITest {

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @Test
    public void collectLogFiles() {
        AsadminResult result = ASADMIN.exec("collect-log-files");
        assertThat(result, asadminOK());
        String path = StringUtils.substringBetween(result.getStdOut(), "Created Zip file under ", ".\n");
        assertNotNull(path, () -> "zip file path parsed from " + result.getStdOut());
        File zipFile = new File(path);
        assertAll(
            () -> assertThat(new File(path).length(), greaterThan(2_000L)),
            () -> assertThat(zipFile.getName(), endsWith(".zip"))
        );
    }


    @Test
    public void listLogLevels() {
        AsadminResult result = ASADMIN.exec("list-log-levels");
        assertThat(result, asadminOK());
        String[] lines = substringBefore(result.getStdOut(), "Command list-log-levels executed successfully.").split("\n");
        assertThat(lines, arrayWithSize(greaterThan(75)));
        Arrays.stream(lines).forEach(line -> {
            String[] pair = line.split("\\s+");
            assertAll(
                () -> assertNotNull(pair[0]),
                () -> assertDoesNotThrow(() -> Level.parse(replaceChars(pair[1], "<>", "")))
            );
        });
    }


    @Test
    public void listLoggers() {
        AsadminResult result = ASADMIN.exec("list-loggers");
        assertThat(result, asadminOK());
        String[] lines = substringBefore(result.getStdOut(), "Command list-loggers executed successfully.").split("\n");
        assertAll(
            () -> assertThat(lines, arrayWithSize(equalTo(61))),
            () -> assertThat(lines[0], matchesPattern("Logger Name[ ]+Subsystem[ ]+Logger Description[ ]+"))
        );
        Map<String, String[]> loggers = Arrays.stream(lines).skip(1).map(line -> line.split("\\s{2,}"))
            .collect(Collectors.toMap(line -> line[0], line -> new String[] {line[1], line[2]}));
        for (Map.Entry<String, String[]> logger : loggers.entrySet()) {
            assertAll(
                () -> assertThat(logger.getKey(), matchesPattern("[a-z\\.]+")),
                () -> assertThat(logger.getValue()[0], matchesPattern("[a-zA-Z_ \\-]+")),
                () -> assertThat(logger.getValue()[1], matchesPattern("[a-zA-Z0-9 \\.\\/\\-]+"))
            );
        }
    }


    @Test
    public void listLogAttributes() {
        AsadminResult result = ASADMIN.exec("list-log-attributes");
        assertThat(result, asadminOK());
        String[] lines = substringBefore(result.getStdOut(), "Command list-log-attributes executed successfully.").split("\n");
        assertThat(lines, arrayWithSize(greaterThan(25)));
        Map<String, String> map = Arrays.stream(lines).map(line -> line.split("\\s+"))
            .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));
        assertAll(
            () -> assertThat(map.get("handlers"),
                equalTo("<org.glassfish.main.jul.handler.GlassFishLogHandler,"
                + "org.glassfish.main.jul.handler.SimpleLogHandler,"
                + "org.glassfish.main.jul.handler.SyslogHandler>")),
            () -> assertThat(map.get("java.util.logging.FileHandler.pattern"), equalTo("<%h/java%u.log>")),
            () -> assertThat(map.get("org.glassfish.main.jul.handler.GlassFishLogHandler.file"),
                equalTo("<${com.sun.aas.instanceRoot}/logs/server.log>"))
        );
    }


    @Test
    public void setLogAttributes() throws Exception {
        AsadminResult result = ASADMIN.exec("set-log-attributes", "--target", "server",
            "org.glassfish.main.jul.handler.GlassFishLogHandler.rotation.maxArchiveFiles=100"
                + ":org.glassfish.main.jul.handler.GlassFishLogHandler.rotation.limit.minutes=240");
        assertThat(result, asadminOK());
        File serverLogFile = new File(getDomain1Directory().resolve("logs").toFile(), "server.log");
        assertThat("lastModified", serverLogFile,
            getterMatches(File::lastModified, lessThan(System.currentTimeMillis() - 100L)));

        File loggingProperties = new File(getDomain1Directory().resolve("config").toFile(), "logging.properties");
        assertThat(loggingProperties, TextFileMatchers.hasLineCount(greaterThan(50L)));

        List<String> lines = Files.lines(loggingProperties.toPath()).collect(Collectors.toList());
        List<String> keys = lines.stream().filter(line -> !line.startsWith("#"))
            .map(line -> line.replaceFirst("=.*", "")).collect(Collectors.toList());
        assertAll(
            () -> assertThat(keys.get(0), equalTo(".level")),
            () -> assertThat(keys.get(keys.size() - 1), equalTo("systemRootLogger.level")),
            () -> {
                String previousKey = "";
                for (String key : keys) {
                    assertThat(key, greaterThan(previousKey));
                    previousKey = key;
                }
            }
        );
    }


    @Test
    public void rotateLog() {
        File serverLogFile = new File(getDomain1Directory().resolve("logs").toFile(), "server.log");
        assertThat(serverLogFile, TextFileMatchers.hasLineCount(greaterThan(50L)));
        AsadminResult result = ASADMIN.exec("rotate-log");
        assertThat(result, asadminOK());
        assertAll(
            () -> assertThat(serverLogFile, FileMatchers.anExistingFile()),
            () -> assertThat(serverLogFile, TextFileMatchers.hasLineCount(0L))
        );
    }

}
