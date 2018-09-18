/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.deployment.client;

import java.io.File;
import java.util.Properties;
import javax.enterprise.deploy.spi.Target;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tjquinn
 */
public class TestDeploy {

    private static final String APP_NAME = "servletonly";

    public TestDeploy() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /*
     * Note that the two tests below are here as examples of how to use the
     * DeploymentFacility.  In their current form they should not be used
     * as tests, because they would require the server to be up.
     */
    @Ignore 
    @Test
    public void testDeploy() {
        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
        ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
        sci.setHostName("localhost");
        sci.setHostPort(4848); // 8080 for the REST client
        sci.setUserName("admin");
        sci.setPassword("");
        
        df.connect(sci);
        
        File archive = new File("/home/hzhang/deployment/apps/jsr88/servletonly-portable.war");
        File plan = new File("/home/hzhang/deployment/apps/jsr88/servletonly-deployplan.jar");
        DFDeploymentProperties options = new DFDeploymentProperties();
        options.setForce(true);
        options.setUpload(true);
        options.setName(APP_NAME);
        Properties props = new Properties();
        props.setProperty("keepSessions", "true");
        props.setProperty("foo", "bar");
        options.setProperties(props);

        try {
        Target[] targets = df.listTargets(); 
        DFProgressObject prog = df.deploy(
                targets /* ==> deploy to the default target */, 
                archive.toURI(), 
                plan.toURI(),
                options);
        DFDeploymentStatus ds = prog.waitFor();
        
        if (ds.getStatus() == DFDeploymentStatus.Status.FAILURE) {
            fail(ds.getAllStageMessages());
        }
        } catch (Exception e) {
          e.printStackTrace();
          fail(e.getMessage());
        }
        
    }
    
    @Ignore
    @Test
    public void testUndeploy() {
        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
        ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
        sci.setHostName("localhost");
        sci.setHostPort(4848); 
        sci.setUserName("admin");
        sci.setPassword("");
        
        df.connect(sci);
        
        try{
        Target[] targets = df.listTargets(); 
        Target[] clusterTarget = new Target[1];
        Target[] dasTarget = new Target[1];
        for (Target target : targets) {
            if (target.getName().equals("server")) {
                dasTarget[0] = target;
            } else if (target.getName().equals("cluster1")) {
                clusterTarget[0] = target;
            }
        }

/* test negative case 
        DFProgressObject prog = df.undeploy(
                clusterTarget, 
                APP_NAME);
*/

        DFProgressObject prog = df.undeploy(
                targets, 
                APP_NAME);

        DFDeploymentStatus ds = prog.waitFor();
        
        if (ds.getStatus() == DFDeploymentStatus.Status.FAILURE) {
            fail(ds.getAllStageMessages());
        }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
    }
}
