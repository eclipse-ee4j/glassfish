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

package org.glassfish.tests.embedded.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletRegistration;
import org.glassfish.embeddable.*;
import org.glassfish.embeddable.web.*;
import org.glassfish.embeddable.web.config.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for Context addServlet & addFilter using class name to default virtual server
 * 
 * @author Amy Roh
 */
public class EmbeddedAddServletAndFilterByClassNameTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static File root;
    static String vsname = "test-server";
    static String contextRoot = "test";

    @BeforeClass
    public static void setupServer() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedAddServletAndFilterByClassNameTest Test");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
        WebContainerConfig config = new WebContainerConfig();
        config.setListings(true);
        root = new File("target/classes");
        config.setDocRootDir(root);
        config.setPort(8080);
        System.out.println("Added Web with base directory "+root.getAbsolutePath());
        embedded.setConfiguration(config);
    }
    
    @Test
    public void testEmbeddedAddServletDefaultVS() throws Exception {

        VirtualServer vs = embedded.getVirtualServer("server");
        System.out.println("Default virtual server "+vs);
        Context context = (Context) embedded.createContext(root);

        ServletRegistration sr = context.addServlet("NewFilterServlet", "org.glassfish.tests.embedded.web.NewFilterServlet");
        sr.setInitParameter("servletInitName", "servletInitValue");
        sr.addMapping("/newFilterServlet");

        FilterRegistration fr = context.addFilter("NewFilter", "org.glassfish.tests.embedded.web.NewFilter");
        fr.setInitParameter("filterInitName", "filterInitValue");
        fr.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST),
                                     true, "NewFilterServlet");

        vs.addContext(context, contextRoot);

        URL servlet = new URL("http://localhost:8080/"+contextRoot+"/newFilterServlet");
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

        vs.removeContext(context);
        
     }

    @AfterClass
    public static void shutdownServer() throws GlassFishException {
        System.out.println("Stopping server " + glassfish);
        if (glassfish != null) {
            glassfish.stop();
            glassfish.dispose();
            glassfish = null;
        }
    }
    
    
}
