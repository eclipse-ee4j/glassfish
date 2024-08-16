/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.distributions.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.distributions.test.ejb.SampleEjb;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.web.HttpListener;
import org.glassfish.embeddable.web.WebContainer;
import org.glassfish.embeddable.web.WebListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EmbeddedTest {

    static GlassFish glassfish;

    @BeforeAll
    public static void setup() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();

        WebContainer webcontainer =
                glassfish.getService(WebContainer.class);
        Collection<WebListener> listeners = webcontainer.getWebListeners();
        System.out.println("Network listener size before creation " + listeners.size());
        for (WebListener listener : listeners) {
            System.out.println("Network listener " + listener.getPort());
        }

        try {
            HttpListener listener = new HttpListener();
            listener.setPort(8080);
            listener.setId("embedded-listener-1");
            webcontainer.addWebListener(listener);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        listeners = webcontainer.getWebListeners();
        System.out.println("Network listener size after creation " + listeners.size());
        Assertions.assertTrue(listeners.size() == 1);
        for (WebListener listener : listeners) {
            System.out.println("Network listener " + listener.getPort());
        }

    }

    @org.junit.jupiter.api.Test
    public void testAll() throws GlassFishException {
        /*
        Set<Sniffer> sniffers = new HashSet<Sniffer>();
        for (EmbeddedContainer c : server.getContainers()) {
            sniffers.addAll(c.getSniffers());
        }
        System.out.println("Sniffer size "  + sniffers.size());
        for (Sniffer sniffer : sniffers) {
            System.out.println("Registered Sniffer " + sniffer.getModuleType());
        }
        */
    }

    @Test
    public void testEjb() throws GlassFishException {
        Deployer deployer = glassfish.getDeployer();

        URL source = SampleEjb.class.getClassLoader().getResource(
                "org/glassfish/distributions/test/ejb/SampleEjb.class");
        String p = source.getPath().substring(0, source.getPath().length() -
                "org/glassfish/distributions/test/ejb/SimpleEjb.class".length());

        String appName = deployer.deploy(new File(p).toURI(), "--name=sample");
        Assertions.assertNotNull("AppName is null from deployer of type " + deployer.getClass().getName(),
                appName);

        // ok now let's look up the EJB...
        try {
            InitialContext ic = new InitialContext();
            SampleEjb ejb = (SampleEjb) ic.lookup("java:global/sample/SampleEjb");
            if (ejb != null) {
                try {
                    System.out.println(ejb.saySomething());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        deployer.undeploy(appName);
        System.out.println("Done with EJB");
    }

    @Test
    public void testWeb() throws GlassFishException {
        System.out.println("Starting testWeb " + glassfish);

        Deployer deployer = glassfish.getDeployer();

        URL source = SampleEjb.class.getClassLoader().getResource(
                "org/glassfish/distributions/test/web/WebHello.class");
        String p = source.getPath().substring(0, source.getPath().length() -
                "org/glassfish/distributions/test/web/WebHello.class".length());

        File path = new File(p).getParentFile().getParentFile();

        String name = null;

        if (path.getName().lastIndexOf('.') != -1) {
            name = path.getName().substring(0, path.getName().lastIndexOf('.'));
        } else {
            name = path.getName();
        }

        System.out.println("Deploying " + path + ", name = " + name);

        String appName = deployer.deploy(path.toURI(), "--name=" + name);

        System.out.println("Deployed " + appName);

        Assertions.assertTrue(appName != null);

        try {
            URL servlet = new URL("http://localhost:8080/test-classes/hello");
            URLConnection yc = servlet.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine = in.readLine();
            if (inputLine != null) {
                System.out.println(inputLine);
            }
            Assertions.assertNotNull(inputLine);
            Assertions.assertEquals(inputLine.trim(), "Hello World !");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            // do not throw the exception for now, because this may break the build if,
            // for example, another instance of glassfish is running on 8080
            //   throw e;
        }

        if (appName != null) {
            deployer.undeploy(appName);
            System.out.println("Undeployed " + appName);
        }

    }

    @Test
    public void commandTest() throws GlassFishException {
        CommandRunner commandRunner = glassfish.getCommandRunner();

        CommandResult commandResult = commandRunner.run("list-modules");
        System.out.println("list-modules command result :\n" + commandResult.getOutput());

        // Unknown commands throw NPE, uncomment once the issue is fixed.
        //commandResult = commandRunner.run("list-contracts");
        //System.out.println("list-contracts command result :\n" + commandResult.getOutput());
    }

    @AfterAll
    public static void close() throws GlassFishException {
        System.out.println("Stopping server " + glassfish);
        if (glassfish != null) {
            glassfish.stop();
            glassfish.dispose();
            glassfish = null;
        }
    }
}
