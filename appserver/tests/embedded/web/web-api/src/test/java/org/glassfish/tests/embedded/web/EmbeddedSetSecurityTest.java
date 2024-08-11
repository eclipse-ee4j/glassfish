/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.web.Context;
import org.glassfish.embeddable.web.WebContainer;
import org.glassfish.embeddable.web.config.AuthMethod;
import org.glassfish.embeddable.web.config.FormLoginConfig;
import org.glassfish.embeddable.web.config.LoginConfig;
import org.glassfish.embeddable.web.config.SecurityConfig;
import org.glassfish.embeddable.web.config.SecurityConstraint;
import org.glassfish.embeddable.web.config.WebContainerConfig;
import org.glassfish.embeddable.web.config.WebResourceCollection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests Context#setSecurity
 *
 * @author Amy Roh
 */
public class EmbeddedSetSecurityTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static File root;
    static String contextRoot = "security";

    @BeforeAll
    public static void setupServer() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedSetSecurity Test");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
        WebContainerConfig config = new WebContainerConfig();
        root = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        //root = new File("/tests/security");
        config.setDocRootDir(root);
        config.setListings(true);
        config.setPort(8080);
        System.out.println("Added Web with base directory "+root.getAbsolutePath());
        embedded.setConfiguration(config);
    }

    @Test
    public void test() throws Exception {

        try {

        Context context = embedded.createContext(root);
        embedded.addContext(context, contextRoot);

        FormLoginConfig form = new FormLoginConfig("/login.html", "/error.html");

        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setAuthMethod(AuthMethod.FORM);
        loginConfig.setRealmName("default");
        loginConfig.setFormLoginConfig(form);

        WebResourceCollection webResource = new WebResourceCollection();
        webResource.setName("ServletTest");
        Set<String> urlPatterns = new HashSet<String>();
        urlPatterns.add("/*");
        webResource.setUrlPatterns(urlPatterns);
        Set<String> httpMethods = new HashSet<String>();
        httpMethods.add("GET");
        httpMethods.add("POST");
        webResource.setHttpMethods(httpMethods);
        // This should throw Exception if uncommented
        //webResource.setHttpMethodOmissions(httpMethods);

        SecurityConstraint securityConstraint = new SecurityConstraint();
        Set<WebResourceCollection> webResources = new HashSet<WebResourceCollection>();
        webResources.add(webResource);
        securityConstraint.setWebResourceCollection(webResources);
        securityConstraint.setAuthConstraint("administrator");
        //securityConstraint.setUserDataConstraint(TransportGuarantee.NONE);

        SecurityConfig securityConfig = new SecurityConfig();
        securityConfig.setLoginConfig(loginConfig);
        Set<SecurityConstraint> securityConstraints = new HashSet<SecurityConstraint>();
        securityConstraints.add(securityConstraint);
        securityConfig.setSecurityConstraints(securityConstraints);

        context.setSecurityConfig(securityConfig);

          /*
        URL servlet = new URL("http://localhost:8080/"+contextRoot+"/ServletTest");
        URLConnection yc = servlet.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                yc.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            sb.append(inputLine);
        }
        in.close();   */

        embedded.removeContext(context);

        } catch (Exception ex) {
            //ignore for now
            //ex.printStackTrace();
        }

    }

    @AfterAll
    public static void shutdownServer() throws GlassFishException {
        System.out.println("Stopping server " + glassfish);
        if (glassfish != null) {
            glassfish.stop();
            glassfish.dispose();
            glassfish = null;
        }
    }

}
