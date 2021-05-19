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

package org.glassfish.tests.weball;

import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.internal.embedded.*;
import org.glassfish.api.embedded.web.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.glassfish.api.admin.ServerEnvironment;

/**
 * @author Vivek Pandey
 */
public class WebAllTest {
    private static Server server = null;
    private static Port http=null;

    @BeforeClass
    public static void setup() throws IOException {

        //create directory 'glassfish' inside target so that it gets cleaned by itself
        EmbeddedFileSystem.Builder fsBuilder = new EmbeddedFileSystem.Builder();
        String p = System.getProperty("buildDir");
        File root = new File(p).getParentFile();
        root =new File(root, "glassfish");
        //If web container requires docroot to be there may be it should be automatically created by embedded API
        new File(root, "docroot").mkdirs();

        EmbeddedFileSystem fs = fsBuilder.instanceRoot(root).build();
        Server.Builder builder = new Server.Builder("WebAllTest");
        builder.embeddedFileSystem(fs);
        server = builder.build();
        server.getHabitat().getService(NetworkConfig.class,
                ServerEnvironment.DEFAULT_INSTANCE_NAME);
        http = server.createPort(8080);
        Assert.assertNotNull("Failed to create port 8080!", http);
        ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);
        EmbeddedWebContainer embedded = (EmbeddedWebContainer) b.create(server);
        embedded.setConfiguration((WebBuilder)b);
        embedded.bind(http, "http");

    }

    @Test
    public void testWeb() throws Exception {
        System.out.println("Starting Web " + server);
        ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);

        System.out.println("builder is " + b);
        server.addContainer(b);
        EmbeddedDeployer deployer = server.getDeployer();
        System.out.println("Added Web");

        String p = System.getProperty("buildDir");
        System.out.println("Root is " + p);
        ScatteredArchive.Builder builder = new ScatteredArchive.Builder("sampleweb", new File(p));
        builder.resources(new File(p));
        builder.addClassPath((new File(p)).toURL());
        DeployCommandParameters dp = new DeployCommandParameters(new File(p));

        System.out.println("Deploying " + p);
        String appName = deployer.deploy(builder.buildWar(), dp);
        Assert.assertNotNull("Deployment failed!", appName);

        URL servlet = new URL("http://localhost:8080/classes/hello");
        URLConnection yc = servlet.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                yc.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            sb.append(inputLine);
        }
        in.close();
        System.out.println(inputLine);

        Assert.assertEquals("Hello World!", sb.toString());

        deployer.undeploy(appName, null);
    }
}
