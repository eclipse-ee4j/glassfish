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

package org.glassfish.tests.embedded.servlet_runs_admin_cmds;

import junit.framework.Assert;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.junit.Test;

import javax.naming.InitialContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author bhavanishankar@dev.java.net
 */

public class RunAdminCommandsTest {

    @Test
    public void test() throws Exception {

        GlassFishProperties props = new GlassFishProperties();
        props.setPort("http-listener", 9090);
        GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish(props);
        glassfish.start();

        // Bind the command runner in JNDI tree with your own mapped-name.
        CommandRunner cr = glassfish.getCommandRunner();
        new InitialContext().bind("org.glassfish.embeddable.CommandRunner", cr);

        // Deploy archive
        Deployer deployer = glassfish.getDeployer();
        String appname = deployer.deploy(new File("target/servlet_runs_admin_cmds.war"));
        System.out.println("Deployed [" + appname + "]");
        Assert.assertEquals(appname, "servlet_runs_admin_cmds");

        get("http://localhost:9090/servlet_runs_admin_cmds",
                "JDBC connection pool sample_derby_pool created successfully");

        deployer.undeploy(appname);
        glassfish.dispose();
    }

    private void get(String urlStr, String result) throws Exception {
        URL url = new URL(urlStr);
        URLConnection yc = url.openConnection();
        System.out.println("\nURLConnection [" + yc + "] : ");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String line = null;
        boolean found = false;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
            if (line.indexOf(result) != -1) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        System.out.println("\n***** SUCCESS **** Found [" + result + "] in the response.*****\n");
    }

}
