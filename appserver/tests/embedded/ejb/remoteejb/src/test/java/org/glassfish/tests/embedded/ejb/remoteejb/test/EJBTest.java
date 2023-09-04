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

package org.glassfish.tests.embedded.ejb.remoteejb.test;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.tests.embedded.ejb.remoteejb.RemoteEJBInf;
import org.glassfish.tests.embedded.ejb.remoteejb.SampleEjb;
import org.glassfish.tests.embedded.ejb.remoteejb.TimerEjb;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.naming.InitialContext;
import java.io.File;
import java.net.URI;

/**
 * @author bhavanishankar@java.net
 */
public class EJBTest {

    /*
        public static void main(String[] args) {
            EmbeddedTest test = new EmbeddedTest();
            System.setProperty("basedir", System.getProperty());
            test.test();
        }
    */
    GlassFish glassfish;

    @Test
    public void test() throws Exception {

        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();


        Deployer deployer = glassfish.getDeployer();
        URI uri = new File(System.getProperty("user.dir"), "target/remoteejb.jar").toURI();
        System.out.println("Deploying [" + uri + "]");
        deployer.deploy(uri);

        InitialContext ic = new InitialContext();

        System.out.println("Looking up SampleEJB.");
        SampleEjb sampleEjb = (SampleEjb) ic.lookup("java:global/remoteejb/SampleEjb");
        System.out.println("Invoking SampleEjb [" + sampleEjb + "]");
        Assertions.assertEquals(sampleEjb.saySomething(), "Hello World");
        System.out.println("SampleEjb tested successfully");

        System.out.println("Looking up TimerEjb.");
        TimerEjb timerEjb = (TimerEjb) ic.lookup("java:global/remoteejb/TimerEjb");
        System.out.println("Invoking TimerEjb [" + timerEjb + "]");
        timerEjb.createTimer();
        System.out.println("Verifying TimerEjb [" + timerEjb + "]");
        Thread.sleep(4000);
        boolean result = timerEjb.verifyTimer();
        Assertions.assertTrue(result);
        System.out.println("TimerEJB tested successfully.");


//        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
//        try {
            System.out.println("Looking up RemoteEJB.");
//            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            RemoteEJBInf remoteEjb = (RemoteEJBInf) ic.lookup("java:global/remoteejb/RemoteEJB");
            System.out.println("Invoking RemoteEJB [" + remoteEjb + "]");
            Assertions.assertEquals(remoteEjb.sayHi(), "Hi Bhavani");
            System.out.println("RemoteEjb tested successfully");
//        } finally {
//            Thread.currentThread().setContextClassLoader(oldCL);
//        }

        glassfish.stop();
        glassfish.dispose();

        System.out.println("EmbeddedTest completed.");

    }

    @Test
    public void test2() throws Exception {

    }

}
