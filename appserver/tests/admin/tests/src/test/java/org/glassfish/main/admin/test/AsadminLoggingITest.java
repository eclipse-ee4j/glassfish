/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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
import java.io.FileReader;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.hamcrest.io.FileMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.replaceChars;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getDomain1Directory;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.glassfish.tests.utils.junit.matcher.DirectoryMatchers.hasEntryCount;
import static org.glassfish.tests.utils.junit.matcher.TextFileMatchers.hasLineCount;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
@TestMethodOrder(OrderAnnotation.class)
public class AsadminLoggingITest {

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @BeforeAll
    public static void fillUpServerLog() {
        // Fill up the server log.
        AsadminResult result = ASADMIN.exec("restart-domain", "--timeout", "60");
        assertThat(result, asadminOK());
    }

    @Test
    @Order(1)
    public void collectLogFiles() {
        AsadminResult result = ASADMIN.exec("collect-log-files");
        assertThat(result, asadminOK());
        String path = StringUtils.substringBetween(result.getStdOut(), "Created Zip file under ", ".zip.") + ".zip";
        assertNotNull(path, () -> "zip file path parsed from " + result.getStdOut());
        File zipFile = new File(path);
        assertTrue(zipFile.exists(), () -> "zip file exists: " + zipFile);
        assertAll(
            () -> assertThat(zipFile.length(), greaterThan(2_000L)),
            () -> assertThat(zipFile.getName(), endsWith(".zip"))
        );
    }


    @Test
    @Order(2)
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


    /**
     * See LoggerInfoMetadataService class, it uses a HK2 registry based on generated
     * META-INF/loggerinfo/LoggerInfoMetadata.properties files.
     */
    @Test
    @Order(3)
    public void listLoggers() {
        AsadminResult result = ASADMIN.exec("list-loggers");
        assertThat(result, asadminOK());
        String[] lines = substringBefore(result.getStdOut(), "Command list-loggers executed successfully.").split("\n");
        assertAll(
            () -> assertThat(lines, arrayWithSize(equalTo(62))),
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
    @Order(4)
    public void listLogAttributes() {
        AsadminResult result = ASADMIN.exec("list-log-attributes");
        assertThat(result, asadminOK());
        String[] lines = substringBefore(result.getStdOut(), "Command list-log-attributes executed successfully.").split("\n");
        assertThat(lines, arrayWithSize(greaterThan(25)));
        Map<String, String> map = Arrays.stream(lines).map(line -> line.split("\\s+"))
            .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));
        assertAll(
            () -> assertThat(map.get("handlers"),
                equalTo("<org.glassfish.main.jul.handler.SimpleLogHandler,"
                + "org.glassfish.main.jul.handler.GlassFishLogHandler,"
                + "org.glassfish.main.jul.handler.SyslogHandler>")),
            () -> assertThat(map.get("org.glassfish.main.jul.handler.GlassFishLogHandler.file"),
                equalTo("<" + INSTANCE_ROOT.toExpression() + "/logs/server.log>"))
        );
    }


    @Test
    @Order(5)
    public void setLogAttributes() throws Exception {
        AsadminResult result = ASADMIN.exec("set-log-attributes", "--target", "server",
            "org.glassfish.main.jul.handler.GlassFishLogHandler.rotation.maxArchiveFiles=100"
                + ":org.glassfish.main.jul.handler.GlassFishLogHandler.rotation.limit.minutes=240");
        assertThat(result, asadminOK());

        File loggingProperties = new File(getDomain1Directory().resolve("config").toFile(), "logging.properties");
        assertThat(loggingProperties, hasLineCount(greaterThan(50L), ISO_8859_1));

        List<String> lines;
        try (Stream<String> lineStream = Files.lines(loggingProperties.toPath())) {
            lines = lineStream.collect(Collectors.toList());
        }

        List<String> keys = lines.stream().filter(line -> !line.startsWith("#"))
            .map(line -> line.replaceFirst("=.*", "")).collect(Collectors.toList());

        assertAll(
            () -> assertThat(keys.get(0), equalTo(".level")),
            () -> assertThat(keys.get(keys.size() - 1), equalTo("systemRootLogger.level")),
            () -> assertThat(lines,
                containsInRelativeOrder(
                    // It was set to OFF in 7.0.3 via AdminUI; check for possible side effects
                    "org.glassfish.main.jul.handler.GlassFishLogHandler.level=ALL",
                    "org.glassfish.main.jul.handler.GlassFishLogHandler.rotation.limit.minutes=240",
                    "org.glassfish.main.jul.handler.GlassFishLogHandler.rotation.maxArchiveFiles=100")),
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
    @Order(100)
    public void rotateLog() throws Exception {
        File serverLogDirectory = getDomain1Directory().resolve("logs").toFile();
        File serverLogFile = new File(serverLogDirectory, "server.log");

        assertThat(serverLogFile, hasLineCount(greaterThan(50L), UTF_8));

        long logEntryCount = Objects.requireNonNull(serverLogDirectory.list()).length;

        long logLineCount;
        try (LineNumberReader reader = new LineNumberReader(new FileReader(serverLogFile))) {
            logLineCount = reader.lines().count();
        }

        AsadminResult result = ASADMIN.exec("rotate-log");
        assertThat(result, asadminOK());

        assertAll(
            () -> assertThat(serverLogFile, FileMatchers.anExistingFile()),
            () -> assertThat(serverLogFile, hasLineCount(lessThan(logLineCount), UTF_8)),
            () -> assertThat(serverLogDirectory, hasEntryCount(logEntryCount + 1))
        );
    }
}
