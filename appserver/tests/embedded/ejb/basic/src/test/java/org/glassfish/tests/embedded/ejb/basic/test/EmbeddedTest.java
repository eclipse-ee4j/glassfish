/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.ejb.basic.test;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.tests.embedded.ejb.basic.SampleEjb;
import org.glassfish.tests.embedded.ejb.basic.TimerEjb;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.naming.InitialContext;
import java.io.File;
import java.net.URI;

/**
 * this test will use the ejb API testing.
 *
 * @author Jerome Dochez
 * @author bhavanishankar@dev.java.net
 */
public class EmbeddedTest {

    @Test
    public void test() throws Exception {
        GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();


        Deployer deployer = glassfish.getDeployer();
        URI uri = new File(System.getProperty("project.directory"), "target/classes").toURI();
        System.out.println("Deploying [" + uri + "]");
        deployer.deploy(uri);

        InitialContext ic = new InitialContext();

        System.out.println("Looking up SampleEJB.");
        SampleEjb sampleEjb = (SampleEjb) ic.lookup("java:global/classes/SampleEjb");
        System.out.println("Invoking SampleEjb [" + sampleEjb + "]");
        Assertions.assertEquals(sampleEjb.saySomething(), "Hello World");
        System.out.println("SampleEjb tested successfully");

        System.out.println("Looking up TimerEjb.");
        TimerEjb timerEjb = (TimerEjb) ic.lookup("java:global/classes/TimerEjb");
        System.out.println("Invoking TimerEjb [" + timerEjb + "]");
        timerEjb.createTimer();
        System.out.println("Verifying TimerEjb [" + timerEjb + "]");
        Thread.sleep(4000);
        boolean result = timerEjb.verifyTimer();
        Assertions.assertTrue(result);
        System.out.println("TimerEJB tested successfully.");

        glassfish.stop();
        glassfish.dispose();

        System.out.println("EmbeddedTest completed.");

    }
}
