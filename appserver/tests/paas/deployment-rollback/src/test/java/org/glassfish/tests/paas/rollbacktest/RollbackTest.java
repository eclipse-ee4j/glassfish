/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.paas.rollbacktest;

import junit.framework.Assert;
import org.glassfish.embeddable.*;
import org.glassfish.paas.orchestrator.provisioning.util.FailureInducer;
import org.glassfish.paas.orchestrator.state.*;
import org.junit.Test;

import java.io.File;
import java.io.PrintStream;

public class RollbackTest {

    private File archive = new File(System.getProperty("basedir")
            + "/target/rollback-test-sample.war"); // TODO :: use mvn apis
    // to get the archive
    // location.
    private GlassFish glassfish = null;
    String appName = "rollback-test";
    boolean initialized = false;

    public void initialize() throws Exception {
        if (!initialized) {
            // 1. Bootstrap GlassFish DAS in embedded mode.
            GlassFishProperties glassFishProperties = new GlassFishProperties();
            glassFishProperties.setInstanceRoot(System.getenv("S1AS_HOME")
                    + "/domains/domain1");
            glassFishProperties.setConfigFileReadOnly(false);
            glassfish = GlassFishRuntime.bootstrap().newGlassFish(
                    glassFishProperties);
            Assert.assertNotNull(glassfish);
            PrintStream sysout = System.out;
            glassfish.start();
            System.setOut(sysout);
            initialized = true;
        }
    }

    @Test
    public void test() throws Exception {

        initialize();
        // 2. Deploy the PaaS application.
        Assert.assertTrue(archive.exists());

        testDeploymentRollback();
        testEnableRollback();
        testDisableRollback();
        tearDown();
    }

    public void testDeploymentRollback() throws Exception {
        Class[] states = {ServiceDependencyDiscoveryState.class, ProvisioningState.class, PreDeployAssociationState.class,
                PostDeployAssociationState.class, DeploymentCompletionState.class};

        for (Class state : states) {
            testRollback(glassfish, archive, appName, state);
        }
    }

    public void testEnableRollback() throws Exception {
        Class[] states = {ServiceDependencyDiscoveryState.class, EnableState.class, PostEnableState.class};
        FailureInducer.setFailureState(null);

        System.out.println("Archive absolute path : " + archive.getAbsolutePath());
        CommandRunner commandRunner = glassfish.getCommandRunner();
        CommandResult result = commandRunner.run("deploy", "--name=" + appName, archive.getAbsolutePath());
        System.out.println("Deploy command result : " + result.getOutput());
        System.out.println("Deploy command exit-status : " + result.getExitStatus());
        System.out.println("Deploy command failure-cause : " + result.getFailureCause());
        Assert.assertTrue(result.getExitStatus() == CommandResult.ExitStatus.SUCCESS);

        result = commandRunner.run("disable", appName);

        System.out.println("disable command result : " + result.getOutput());
        System.out.println("disable command exit-status : " + result.getExitStatus());
        System.out.println("disable command failure-cause : " + result.getFailureCause());
        Assert.assertTrue(result.getExitStatus() == CommandResult.ExitStatus.SUCCESS);

        result = commandRunner.run("list-services", "appname=" + appName, "output=STATE");
        System.out.println("list-services --appname=[" + appName + "] : status : " + result.getExitStatus());
        System.out.println("\nlist-services command output [ "
                + result.getOutput() + "]");
        boolean notRunning = result.getOutput().toLowerCase().contains("notrunning");
        boolean stopped = result.getOutput().toLowerCase().contains("stopped");
        Assert.assertTrue(stopped || notRunning);

        for (Class state : states) {
            try {
                System.out.println("Setting failure inducer with state : " + state.getSimpleName());
                FailureInducer.setFailureState(state);
                result = commandRunner.run("enable", appName);

                System.out.println("enable command result : " + result.getOutput());
                System.out.println("enable command exit-status : " + result.getExitStatus());
                System.out.println("enable command failure-cause : " + result.getFailureCause());
                Assert.assertTrue(result.getExitStatus() == CommandResult.ExitStatus.FAILURE);

                result = commandRunner.run("list-services", "appname=" + appName, "output=STATE");
                System.out.println("list-services --appname=[" + appName + "] : status : " + result.getExitStatus());
                System.out.println("\nlist-services command output [ "
                        + result.getOutput() + "]");
                notRunning = result.getOutput().toLowerCase().contains("notrunning");
                stopped = result.getOutput().toLowerCase().contains("stopped");
                Assert.assertTrue(stopped || notRunning);


            } catch (Exception gfe) {
                System.out.println("Failure while testing enable-rollback on application [" + archive.getName() + "] " + gfe.getLocalizedMessage());
                gfe.printStackTrace();
            }
        }

        FailureInducer.setFailureState(null);
        result = commandRunner.run("enable", appName);

        System.out.println("enable command result : " + result.getOutput());
        System.out.println("enable command exit-status : " + result.getExitStatus());
        System.out.println("enable command failure-cause : " + result.getFailureCause());
        Assert.assertTrue(result.getExitStatus() == CommandResult.ExitStatus.SUCCESS);

        result = commandRunner.run("list-services", "appname=" + appName, "output=STATE");
        System.out.println("list-services --appname=[" + appName + "] : status : " + result.getExitStatus());
        System.out.println("\nlist-services command output [ "
                + result.getOutput() + "]");
        notRunning = result.getOutput().toLowerCase().contains("notrunning");
        Assert.assertTrue(!notRunning);
        stopped = result.getOutput().toLowerCase().contains("stopped");
        Assert.assertTrue(!stopped);

    }

    public void testDisableRollback() throws Exception {
        Class[] states = {DisableState.class, PostDisableState.class};
        FailureInducer.setFailureState(null);

        CommandRunner commandRunner = glassfish.getCommandRunner();
        CommandResult result = null;
        for (Class state : states) {

            try {
                System.out.println("Setting failure inducer with state : " + state.getSimpleName());
                FailureInducer.setFailureState(state);
                result = commandRunner.run("disable", appName);

                System.out.println("disable command result : " + result.getOutput());
                System.out.println("disable command exit-status : " + result.getExitStatus());
                System.out.println("disable command failure-cause : " + result.getFailureCause());
                Assert.assertTrue(result.getExitStatus() == CommandResult.ExitStatus.FAILURE);

                result = commandRunner.run("list-services", "appname=" + appName, "output=STATE");
                System.out.println("list-services --appname=[" + appName + "] : status : " + result.getExitStatus());
                System.out.println("\nlist-services command output [ "
                        + result.getOutput() + "]");
                boolean notRunning = result.getOutput().toLowerCase().contains("notrunning");
                boolean stopped = result.getOutput().toLowerCase().contains("stopped");
                Assert.assertTrue(!stopped && !notRunning);


            } catch (Exception gfe) {
                System.out.println("Failure while testing disable-rollback on application [" + archive.getName() + "] " + gfe.getLocalizedMessage());
                gfe.printStackTrace();
            }
        }
    }

    public void tearDown() throws Exception {
        FailureInducer.setFailureState(null);
        CommandRunner commandRunner = glassfish.getCommandRunner();
        CommandResult result = commandRunner.run("undeploy", appName);

        System.out.println("undeploy command result : " + result.getOutput());
        System.out.println("undeploy command exit-status : " + result.getExitStatus());
        System.out.println("undeploy command failure-cause : " + result.getFailureCause());
        Assert.assertTrue(result.getExitStatus() == CommandResult.ExitStatus.SUCCESS);
    }


    private void testRollback(GlassFish glassfish, File archive, String appName, Class state) throws GlassFishException {

        CommandRunner commandRunner = glassfish.getCommandRunner();
        try {
            System.out.println("Setting failure inducer with state : " + state.getSimpleName());
            FailureInducer.setFailureState(state);
            System.out.println("Archive absolute path : " + archive.getAbsolutePath());
            CommandResult result = commandRunner.run("deploy", "--name=" + appName, archive.getAbsolutePath());
            System.out.println("Deploy command result : " + result.getOutput());
            System.out.println("Deploy command exit-status : " + result.getExitStatus());
            System.out.println("Deploy command failure-cause : " + result.getFailureCause());

            validateResult(appName, commandRunner);

        } catch (Exception gfe) {
            System.out.println("Failure while deploying application [" + archive.getName() + "] " + gfe.getLocalizedMessage());
            gfe.printStackTrace();
            validateResult(appName, commandRunner);
        }
    }

    private void validateResult(String appName, CommandRunner commandRunner) {
        CommandResult result;
        result = commandRunner.run("list-services", "--appname=" + appName);
        System.out.println("list-services --appname=[" + appName + "] : status : " + result.getExitStatus());
        Assert.assertEquals(result.getExitStatus(), CommandResult.ExitStatus.FAILURE);

        result = commandRunner.run("list-services");
        System.out.println("list-services : status : " + result.getExitStatus());
        boolean containsNothingToList = result.getOutput().contains("Nothing to list");
        Assert.assertTrue(containsNothingToList);

        result = commandRunner.run("list-applications", "domain");
        boolean applicationFound = result.getOutput().contains(appName);
        Assert.assertTrue(!applicationFound);
    }
}
