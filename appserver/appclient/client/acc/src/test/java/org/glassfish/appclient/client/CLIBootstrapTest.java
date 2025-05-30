/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Tim Quinn
 */
public class CLIBootstrapTest {

    @BeforeEach
    public void setUp() {
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + "AS_JAVA", "");
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + "JAVA_HOME", "");
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + "PATH", System.getenv("PATH"));
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + "AS_INSTALL",
            "/Users/Tim/asgroup/v3/H/publish/glassfish8/glassfish");
    }

    @Test
    public void testChooseJavaASJAVAAsCurrent() {
        runTest("AS_JAVA");
    }

    @Test
    public void testChooseJavaJAVAHOMEAsCurrent() {
        runTest("JAVA_HOME");
    }


    @Test
    public void testChooseJavaASJAVAAsBad() {
        runTestUsingBadLocation("AS_JAVA");
    }

    @Test
    public void testChooseJAVAHOMEAsBad() {
        runTestUsingBadLocation("JAVA_HOME");
    }

    private void runTestUsingBadLocation(final String envVarName) {
        final CLIBootstrap boot = assertDoesNotThrow(() -> new CLIBootstrap());
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + envVarName, "shouldnotexistanywhere");
        CLIBootstrap.JavaInfo javaInfo = boot.initJava();
        assertNotNull(javaInfo);
    }

    private void runTest(final String envVarName) {
        String javaHome = System.getProperty("java.home");
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + envVarName, javaHome);
        final CLIBootstrap boot = assertDoesNotThrow(() -> new CLIBootstrap());
        CLIBootstrap.JavaInfo javaInfo = boot.initJava();
        assertAll(
            () -> assertNotNull(javaInfo, "found no match; expected to match on " + envVarName),
            () -> assertEquals(javaHome, javaInfo.javaBinDir().getAbsoluteFile().getParent()),
            () -> assertFalse(javaInfo.isValid(), "it should have been valid, derived as it was from PATH, but was not")
        );
    }

    @Test
    public void testCommandLineArgument() throws Throwable {
        CLIBootstrap.CommandLineArgument instance = (new CLIBootstrap()).new CommandLineArgument(".*", Pattern.DOTALL);
        String expect = "\"test\" \"append\"";
        StringBuilder preCommandLineArgs = new StringBuilder("\"test\"");
        String appendArg = "append";
        StringBuilder actual = instance.format(preCommandLineArgs, true, appendArg);
        assertEquals(actual.toString(), expect);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void testQuoteEscapedArgument() throws Throwable {
        String expect = "\"a\\\\\\\"b\\\\\\$\\`c\\\"\\\\\"";
        String inputArgs = "a\\\"b\\$`c\"\\";
        String actual = CLIBootstrap.quoteEscapedArgument(inputArgs);
        assertEquals(actual.toString(), expect);
    }

    @Test
    public void testAcceptabilityForEscapedDoubleQuotes() throws Throwable {
        runTestUsingConvertInputArgsVariable("a\\\"b c\\\"d", new String[] { "a\\\"b", "c\\\"d" });
    }

    @Test
    public void testAcceptabilityForQuotedEscapedDoubleQuotes() throws Throwable {
        runTestUsingConvertInputArgsVariable("\"a \\\" b \\\" c\"", new String[] { "a \\\" b \\\" c" });
    }

    @Test
    public void testAcceptabilityForQuotedEmptyStr() throws Throwable {
        runTestUsingConvertInputArgsVariable("\"\"", new String[] { "" });
    }

    private void runTestUsingConvertInputArgsVariable( String inputArgs, String[] expect) throws Throwable {
        Method convertInputArgsVariableMethod = CLIBootstrap.class.getDeclaredMethod("convertInputArgsVariable", String.class);
        convertInputArgsVariableMethod.setAccessible(true);
        String[] actual = (String[]) convertInputArgsVariableMethod.invoke(null, inputArgs);
        assertArrayEquals(actual, expect);
    }
}
