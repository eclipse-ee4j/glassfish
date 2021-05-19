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

package devtests.deployment.ejb.statelesshello.testng;

import devtests.deployment.DeploymentTest;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Property;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * Illustrates an example test relying some of the inherited logic
 * in DeploymentTest.
 *
 * The general flow of this test is:
 *
 *   assemble the jars
 *   deploy the app and run the client
 *   redeploy the app and run the client
 *   undeploy the app
 *   deploy the app using JSR-88 and run the client
 *   redeploy the app using JSR-88 and run the client
 *   undeploy the app using JSR-88
 *
 * @author: tjquinn
 *
 */

public class StatelessHelloTest extends DeploymentTest {

    private int nextLogID = 0;

    /** Creates a new instance of ExampleTest */
    public StatelessHelloTest() {
    }

    /**
     *Deploy the app using asadmin and run it, expecting a positive result.
     */
    @Test
    public void deployWithAsadminAndRun() {
        deploy();
        runPositive("ejb/statelessejb Test asadmin deploy");
    }

    /**
     *Redeploy and run after first test.
     */
    @Test(dependsOnMethods={"deployWithAsadminAndRun"})
    public void redeployWithAsadminAndRun() {
        redeploy();
        runPositive("ejb/statelessejb Test asadmin redeploy");
    }

    /**
     *Undeploy using asadmin after first deployment and redeployment.
     */
    @Test(alwaysRun=true,dependsOnMethods={"redeployWithAsadminAndRun"})
    public void undeployAfterAsadminRuns() {
        undeploy();
    }

    /**
     *Deploy with JSR-88 and run.
     */
    @Test(dependsOnMethods={"undeployAfterAsadminRuns"})
    public void deployWithJSR88AndRun() {
        deployWithJSR88();
        runPositive("ejb/statelessejb Test jsr88 deploy");
    }

    /**
     *Stop using JSR-88 and attempt to run.
     */
    @Test(dependsOnMethods={"deployWithJSR88AndRun"})
    public void stopAndRetry() {
        stopWithJSR88();
        runNegative("ejb/statelessejb Test jsr88 stopped state");
    }

    /**
     *Start with JSR-88 and attempt to run.
     */
    @Test(dependsOnMethods={"stopAndRetry"})
    public void startAndRetry() {
        startWithJSR88();
        runPositive("ejb/statelessejb Test jsr88 started state");
    }

    /**
     *Stop, redeploy, and attempt to run (should fail).
     */
    @Test(dependsOnMethods={"startAndRetry"})
    public void stopRedeployAndRetry() {
        stopWithJSR88();
        redeployWithJSR88();
        runNegative("ejb/statelessejb Test jsr88 redeploy stop");
    }

    @Configuration(afterTestClass=true)
    public void unsetup() {
        undeployAtEnd();
    }

    public void undeployAtEnd() {
        undeployWithJSR88();
    }

    protected void deployWithJSR88() {
        project.executeTarget("deploy.jsr88");
    }

    protected void startWithJSR88() {
        project.executeTarget("start.jsr88");
    }

    protected void stopWithJSR88() {
        project.executeTarget("stop.jsr88");
    }

    protected void redeployWithJSR88() {
        project.executeTarget("redeploy.jsr88");
    }

    protected void undeployWithJSR88() {
        project.executeTarget("undeploy.jsr88");
    }

    protected void runPositive(String testTitle) {
        run(testTitle, "run.positive");
    }

    protected void runNegative(String testTitle) {
        run(testTitle, "run.negative");
    }

    protected void run(String testTitle, String runTarget) {
        CallTarget target = new CallTarget();
        target.setProject(project);
        target.setTarget(runTarget);

        Property logID = target.createParam();
        Property description = target.createParam();
        logID.setName("log.id");
        logID.setValue(String.valueOf(nextLogID++));
        description.setName("description");
        description.setValue(testTitle);

        target.execute();
    }
}
