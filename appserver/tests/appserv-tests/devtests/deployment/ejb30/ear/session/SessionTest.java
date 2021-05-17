/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package devtests.deployment.ejb30.ear.session.testng;

import devtests.deployment.DeploymentTest;
import org.testng.annotations.Test;

/**
 * Illustrates an example test relying some of the inherited logic
 * in DeploymentTest.
 *
 * @author: tjquinn
 *
 */

public class SessionTest extends DeploymentTest {

    /** Creates a new instance of ExampleTest */
    public SessionTest() {
    }

    /**
     *Runs the first step of the test: deploying and running the client.
     */
    @Test
    public void deployAndRun() {
        deploy();
        runClient();
    }

    /**
     *Runs the second step of the test, only after the first has run and
     *succeeded: redeploy the app and run the client again.
     */
    @Test(dependsOnMethods={"deployAndRun"})
    public void redeployAndRun() {
        redeploy();
        runClient();
    }
}
