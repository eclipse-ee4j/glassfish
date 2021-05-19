/*
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

package org.glassfish.tests.embedded.inplanted;

import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.embedded.*;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.admin.*;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.web.EmbeddedWebContainer;
import org.glassfish.tests.embedded.utils.EmbeddedServerUtils;

import java.io.File;
import java.util.Enumeration;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Jerome Dochez
 */
public class InplantedTest {

    static Server server;

    @BeforeClass
    public static void setupServer() throws Exception {
        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        efsb.installRoot(EmbeddedServerUtils.getServerLocation());
        server = EmbeddedServerUtils.createServer(efsb.build());
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
        Port http = server.createPort(8080);
        ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);
        server.addContainer(b);
        EmbeddedWebContainer embedded = (EmbeddedWebContainer) b.create(server);
        embedded.bind(http, "http");

        DeployCommandParameters dp = new DeployCommandParameters(f);
        String appName = server.getDeployer().deploy(war, dp);
        WebClient webClient = new WebClient();
        Page page =  webClient.getPage("http://localhost:8080/test-classes/hello");
        System.out.println("Got response " + page.getWebResponse().getContentAsString());
        Assert.assertTrue("Servlet returned wrong content", page.getWebResponse().getContentAsString().startsWith("Hello World"));
        server.getDeployer().undeploy(appName, null);
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
        EmbeddedServerUtils.shutdownServer(server);
    }
}
