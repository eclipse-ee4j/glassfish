/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.appclient.client.acc.UserError;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Quinn
 */
public class CLIBootstrapTest {

    public CLIBootstrapTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + "AS_JAVA", "");
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + "JAVA_HOME", "");
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + "PATH",
                System.getenv("PATH"));
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + "_AS_INSTALL",
                "/Users/Tim/asgroup/v3/H/publish/glassfish6/glassfish");
    }

    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    public void testChooseJavaASJAVAAsCurrent() {
        runTest("AS_JAVA");
    }

    @Ignore
    @Test
    public void testChooseJavaJAVAHOMEAsCurrent() {
        runTest("JAVA_HOME");
    }


    @Ignore
    @Test
    public void testChooseJavaASJAVAAsBad() {
        runTestUsingBadLocation("AS_JAVA");
    }

    @Ignore
    @Test
    public void testChooseJAVAHOMEAsBad() {
        runTestUsingBadLocation("JAVA_HOME");
    }

    private void runTestUsingBadLocation(final String envVarName) {
        try {
            final CLIBootstrap boot = new CLIBootstrap();
            System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + envVarName,
                        "shouldnotexistanywhere");
            CLIBootstrap.JavaInfo javaInfo = boot.initJava();

        } catch (UserError ex) {
            /*
             * We expect this exception because we tried to use a non-sensical
             * setting for the java location.
             */
        }
    }

    private void runTest(final String envVarName) {
        System.setProperty(CLIBootstrap.ENV_VAR_PROP_PREFIX + envVarName,
                       System.getProperty("java.home"));
        try {
            final CLIBootstrap boot = new CLIBootstrap();
            CLIBootstrap.JavaInfo javaInfo = boot.initJava();
            if (javaInfo == null) {
                fail("chooseJava found no match; expected to match on " + envVarName);
            }
            if ( ! javaInfo.toString().equals(envVarName)) {
                fail("Expected to choose " + envVarName + " but chose " + javaInfo.toString() + " instead");
            }
            if ( ! javaInfo.isValid()) {
                fail("Correctly chose " + envVarName + " but it should have been valid, derived as it was from PATH, but was not");
            }
        } catch (UserError ex) {
            fail(ex.getMessage());
        }
    }

}
