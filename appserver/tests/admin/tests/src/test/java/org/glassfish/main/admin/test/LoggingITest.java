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
import java.util.Arrays;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.main.admin.test.tool.asadmin.Asadmin;
import org.glassfish.main.admin.test.tool.asadmin.AsadminResult;
import org.glassfish.main.admin.test.tool.asadmin.GlassFishTestEnvironment;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.StringUtils.replaceChars;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.glassfish.main.admin.test.tool.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author David Matejcek
 */
public class LoggingITest {

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @Test
    public void collectLogFiles() {
        AsadminResult result = ASADMIN.exec("collect-log-files");
        assertThat(result, asadminOK());
        String path = StringUtils.substringBetween(result.getStdOut(), "Created Zip file under ", ".\n");
        assertNotNull(path, () -> "zip file path parsed from " + result.getStdOut());
        assertThat(new File(path).length(), greaterThan(2_000L));
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
}
