/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.cooked;

import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.embedded.*;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.admin.*;
import org.glassfish.api.deployment.DeployCommandParameters;

import java.io.*;
import java.util.Enumeration;

/**
 * @author Jerome Dochez
 */
public class InplantedTest {

    static Server server;

    @BeforeClass
    public static void setupServer() throws Exception {
        System.out.println("setup started with gf installation " + System.getProperty("basedir"));
        File f = new File(System.getProperty("basedir"));
        f = new File(f, "target");
        f = new File(f, "dependency");
        f = new File(f, "glassfish7");
        f = new File(f, "glassfish");
        if (f.exists()) {
            System.out.println("Using gf at " + f.getAbsolutePath());
        } else {
            System.out.println("GlassFish not found at " + f.getAbsolutePath());
            Assert.assertTrue(f.exists());
        }
        try {
            EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
            efsb.installRoot(f, false);
            Server.Builder builder = new Server.Builder("inplanted");
            builder.embeddedFileSystem(efsb.build());
            server = builder.build();
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testWeb() throws Exception {
        System.out.println("test web");
        File f = new File(System.getProperty("basedir"));
        f = new File(f, "target");
        f = new File(f, "test-classes");
        ScatteredArchive.Builder builder = new ScatteredArchive.Builder("hello", f);
        builder.addClassPath(f.toURI().toURL());
        builder.resources(f);
        ScatteredArchive war = builder.buildWar();
        System.out.println("War content");
        Enumeration<String> contents = war.entries();
        while(contents.hasMoreElements()) {
            System.out.println(contents.nextElement());
        }
        try {
            System.out.println("Port created " + server.createPort(14587));
            server.addContainer(ContainerBuilder.Type.web);
            server.start();
            DeployCommandParameters dp = new DeployCommandParameters(f);
            String appName = server.getDeployer().deploy(war, dp);
            System.out.println("Application deployed under name = " + appName);
            if (appName!=null) {
                server.getDeployer().undeploy(appName, null);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (LifecycleException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void Test() {

        ServiceLocator habitat = server.getHabitat();
        System.out.println("Process type is " + habitat.<ProcessEnvironment>getService(ProcessEnvironment.class).getProcessType());
        for (Sniffer s : habitat.<Sniffer>getAllServices(Sniffer.class)) {
            System.out.println("Got sniffer " + s.getModuleType());
        }
    }

    @AfterClass
    public static void shutdownServer() throws Exception {
        System.out.println("shutdown initiated");
        if (server!=null) {
            try {
                server.stop();
            } catch (LifecycleException e) {
                e.printStackTrace();
                throw e;
            }
        }


    }
}
