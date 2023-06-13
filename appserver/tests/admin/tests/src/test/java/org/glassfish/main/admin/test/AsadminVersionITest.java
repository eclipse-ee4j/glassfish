/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opentest4j.MultipleFailuresError;

import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author David Matejcek
 */
@TestMethodOrder(OrderAnnotation.class)
public class AsadminVersionITest {

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @AfterAll
    public static void startDomainAgain() {
        assertThat(ASADMIN.exec("start-domain"), asadminOK());
    }


    @Test
    @Order(1)
    public void version() {
        AsadminResult result = ASADMIN.exec("version");
        assertThat(result, asadminOK());
        String output = result.getStdOut();
        checkOutput(output);
    }


    @Test
    @Order(100)
    public void versionAfterShutdown() {
        assertThat(ASADMIN.exec("stop-domain"), asadminOK());
        AsadminResult result = ASADMIN.exec("version", "--local");
        assertThat(result, asadminOK());
        checkOutput(result.getStdOut());
    }


    private void checkOutput(String output) throws MultipleFailuresError {
        assertThat(output,
            stringContainsInOrder("Version = Eclipse GlassFish ", "commit: ", ", timestamp: "));
        String commit = StringUtils.substringBetween(output, "commit: ", ",");
        String timestamp = StringUtils.substringBetween(output, ", timestamp: ", ")");
        assertAll(
            () -> assertThat(commit, matchesPattern("[a-f0-9]+")),
            () -> assertDoesNotThrow(() -> Instant.parse(timestamp))
        );
    }
}
