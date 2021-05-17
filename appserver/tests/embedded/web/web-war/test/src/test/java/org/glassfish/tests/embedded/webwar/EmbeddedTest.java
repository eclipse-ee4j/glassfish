/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.webwar;

import java.util.*;
import org.junit.Test;
import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.internal.embedded.Server;
import org.glassfish.internal.embedded.LifecycleException;
import org.glassfish.internal.embedded.EmbeddedContainer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.internal.embedded.EmbeddedDeployer;
import org.glassfish.internal.embedded.Port;
import org.glassfish.internal.embedded.ScatteredArchive;
import org.glassfish.internal.embedded.ScatteredArchive.Builder;
import org.glassfish.internal.embedded.ContainerBuilder;
import org.glassfish.internal.embedded.admin.AdminInfo;
import org.glassfish.internal.embedded.admin.EmbeddedAdminContainer;
import org.glassfish.internal.embedded.admin.CommandExecution;
import org.glassfish.internal.embedded.admin.CommandParameters;
import org.glassfish.api.embedded.web.EmbeddedWebContainer;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.container.Sniffer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import org.glassfish.api.admin.ServerEnvironment;

public class EmbeddedTest {

    private static Port http=null;
    private static Server server = null;

    @BeforeClass
    public static void setup() {
        Server.Builder builder = new Server.Builder("build");

        server = builder.build();
        NetworkConfig nc = server.getHabitat().getService(NetworkConfig.class,
                ServerEnvironment.DEFAULT_INSTANCE_NAME);
        List<NetworkListener> listeners = nc.getNetworkListeners().getNetworkListener();
        System.out.println("Network listener size before creation " + listeners.size());
        for (NetworkListener nl : listeners) {
            System.out.println("Network listener " + nl.getPort());
        }
        try {
            http = server.createPort(8080);
            ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);
            server.addContainer(b);
            EmbeddedWebContainer embedded = (EmbeddedWebContainer) b.create(server);
            embedded.bind(http, "http");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        listeners = nc.getNetworkListeners().getNetworkListener();
        System.out.println("Network listener size after creation " + listeners.size());
        Assert.assertTrue(listeners.size() == 1);
        for (NetworkListener nl : listeners) {
            System.out.println("Network listener " + nl.getPort());
        }
        Collection<NetworkListener> cnl = server.getHabitat().getAllServices(NetworkListener.class);
        System.out.println("Network listener size after creation " + cnl.size());
        for (NetworkListener nl : cnl) {
            System.out.println("Network listener " + nl.getPort());
        }

        server.addContainer(ContainerBuilder.Type.all);
    }

    @Test
    public void testWeb() throws Exception {
        System.out.println("Starting Web " + server);
        ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);
        System.out.println("builder is " + b);
        server.addContainer(b);
        EmbeddedDeployer deployer = server.getDeployer();
        System.out.println("Added Web");

        String testClass = "org/glassfish/tests/embedded/webwar/EmbeddedTest.class";
        URL source = this.getClass().getClassLoader().getResource(testClass);
        String p = source.getPath().substring(0, source.getPath().length()-testClass.length()) +
            "../../../war/target/test-war.war";

        System.out.println("Root is " + p);
        DeployCommandParameters dp = new DeployCommandParameters(new File(p));

        System.out.println("Deploying " + p);
        String appName = null;
        try {
            appName = deployer.deploy(new File(p), dp);
            System.out.println("Deployed " + appName);
            Assert.assertTrue(appName != null);
            try {
                URL servlet = new URL("http://localhost:8080/test-war/");
                URLConnection yc = servlet.openConnection();
                BufferedReader in = new BufferedReader(
                                        new InputStreamReader(
                                        yc.getInputStream()));
                String inputLine = in.readLine();
                if (inputLine != null)
                    System.out.println(inputLine);
                Assert.assertEquals(inputLine.trim(), "filterMessage=213");
                in.close();
            } catch(Exception e) {
                e.printStackTrace();
                throw e;
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
        if (appName!=null)
            deployer.undeploy(appName, null);

    }

    public static void  close() throws LifecycleException {
        if (http!=null) {
            http.close();
            http=null;
        }
        System.out.println("Stopping server " + server);
        if (server!=null) {
            server.stop();
            server=null;
        }
    }
}
