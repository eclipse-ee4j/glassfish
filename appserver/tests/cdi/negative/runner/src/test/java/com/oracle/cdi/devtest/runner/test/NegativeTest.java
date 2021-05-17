/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.cdi.devtest.runner.test;

import org.glassfish.tests.utils.NucleusStartStopTest;
import org.glassfish.tests.utils.NucleusTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This tests failure cases
 *
 * @author jwells
 *
 */
public class NegativeTest extends NucleusStartStopTest {
    private final static String NORMAL_WITH_FINALS_JAR = "negative/normalScopeWithFinal/target/normalScopeWithFinal.jar";
    private final static String SOURCE_HOME = System.getProperty("source.home", "$");
    private final static String SOURCE_HOME_CDI = "/appserver/tests/cdi/";

    private static String getDeployablePath(String endPath) {
        if (!SOURCE_HOME.startsWith("$")) {
            return SOURCE_HOME + SOURCE_HOME_CDI + endPath;
        }

        return endPath;
    }

    /**
     * This test currently fails due to a possible bug in Weld
     */
    @Test
    public void testNormalScopeWithFinalsDoesNotDeploy() {
        String deployPath = getDeployablePath(NORMAL_WITH_FINALS_JAR);

        boolean success = NucleusTestUtils.nadmin("deploy", deployPath);

        // Should have failed
        Assert.assertFalse(success);
    }
}
