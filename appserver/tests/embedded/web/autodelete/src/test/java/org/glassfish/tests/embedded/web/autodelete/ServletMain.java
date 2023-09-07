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

package org.glassfish.tests.embedded.web.autodelete;

import org.junit.*;
import org.junit.Assert;
import org.glassfish.internal.embedded.*;
import org.glassfish.api.deployment.*;

import java.io.*;
import java.util.*;

import com.gargoylesoftware.htmlunit.*;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Nov 4, 2009
 * Time: 1:44:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServletMain {

    private static final String PROJECT_DIR = System.getProperty("project.directory");

    public static void main(String[] args) {
        ServletMain test = new ServletMain();
        System.setProperty("basedir", System.getProperty("user.dir"));
        try {
            test.test();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void test() throws Exception {

        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        File instanceRoot = new File(System.getProperty("user.dir"), "embeddedMain");
        System.out.println("Using instanceRoot " + instanceRoot.getAbsolutePath());
        efsb.instanceRoot(instanceRoot).autoDelete(true);

        Server server = new Server.Builder("web").embeddedFileSystem(efsb.build()).build();
        try {
            File f = new File(PROJECT_DIR);
            f = new File(f, "target");
            f = new File(f, "classes");
            ScatteredArchive.Builder builder = new ScatteredArchive.Builder("hello", f);
            builder.addClassPath(f.toURI().toURL());
            builder.resources(f);
            ScatteredArchive war = builder.buildWar();
            System.out.println("War content");
            Enumeration<String> contents = war.entries();
            while(contents.hasMoreElements()) {
                System.out.println(contents.nextElement());
            }
            Port port = server.createPort(8080);
            server.addContainer(server.createConfig(ContainerBuilder.Type.web));
            DeployCommandParameters dp = new DeployCommandParameters(f);
            String appName = server.getDeployer().deploy(war, dp);
            WebClient webClient = new WebClient();
            try {
                Page page =  webClient.getPage("http://localhost:8080/classes/hello");
                System.out.println("Got response " + page.getWebResponse().getContentAsString());
                Assert.assertTrue("Servlet returned wrong content", page.getWebResponse().getContentAsString().startsWith("Hello World"));
            } finally {
                System.out.println("Undeploying");
                server.getDeployer().undeploy(appName, null);
                port.close();
            }
            listDir(instanceRoot);

        } finally {
            System.out.println("Stopping the server !");
            try {
                server.stop();
            } catch (LifecycleException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (instanceRoot.listFiles()!=null) {
            listDir(instanceRoot);
            throw new RuntimeException("some files were not cleaned");
        }
    }

    private void listDir(File dir) {
        if (!dir.exists()) {
            System.out.println("Directory " + dir + " does not exist");
            return;
        }
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                listDir(f);
            } else {
                System.out.println(f.getAbsolutePath());
            }
        }
    }
}
