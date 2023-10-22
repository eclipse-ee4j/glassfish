/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.internal.embedded.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Vivek Pandey
 */
public class WebAllTest {
    private static Server server = null;
    private static Port http=null;

    @BeforeClass
    public static void setup() throws IOException {

        //create directory 'glassfish' inside build directory so that it gets cleaned by Maven
        EmbeddedFileSystem.Builder fsBuilder = new EmbeddedFileSystem.Builder();
        String p = System.getProperty("buildDir");
        File root = p != null ? new File(p) : new File(".").getParentFile();
        root = new File(root, "glassfish");
        //If web container requires docroot to be there may be it should be automatically created by embedded API
        new File(root, "docroot").mkdirs();

        EmbeddedFileSystem fs = fsBuilder.instanceRoot(root).build();
        Server.Builder builder = new Server.Builder("WebAllTest");
        builder.embeddedFileSystem(fs);
        server = builder.build();
        http = server.createPort(8080);
        Assert.assertNotNull("Failed to create port 8080!", http);
        ContainerBuilder<EmbeddedContainer> b = server.createConfig(ContainerBuilder.Type.web);
        EmbeddedContainer embedded = b.create(server);
//        embedded.setConfiguration((WebBuilder)b);
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

        String p = System.getProperty("project.directory");
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
