/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.appclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class AppClientTest {

    @Test
    void noArgsAndHelp() throws Exception {
        File appclient = GlassFishTestEnvironment.getAppClient();
        assertTrue(appclient.canExecute(), "appclient executable");
        Process processNoArgs = new ProcessBuilder(appclient.getAbsolutePath()).start();
        Process processUsage = new ProcessBuilder(appclient.getAbsolutePath(), "-usage").start();
        processNoArgs.waitFor();
        processUsage.waitFor();
        String noArgsOutput = readOutput(processNoArgs);
        String usageOutput = readOutput(processUsage);
        assertEquals(1, processNoArgs.exitValue(), "appclient without args exit code. Output: " + noArgsOutput);
        assertEquals(0, processUsage.exitValue(), "appclient -usage exit code. Output: " + usageOutput);
        assertThat("outputs should be same", usageOutput,
            stringContainsInOrder("appclient [ <classfile> | -client <appjar> ]", "or",
                "appclient [ <valid JVM options and valid ACC options> ]"));
        assertEquals(usageOutput, noArgsOutput, "outputs should be same");
    }


    private String readOutput(Process process) throws IOException {
        try (InputStream output = process.getInputStream()) {
            return new String(output.readAllBytes(), Charset.defaultCharset());
        }
    }
}
