/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
 * MailResourceDeployerTest.java
 *
 * Created on December 10, 2003, 11:55 AM
 */

package com.sun.enterprise.resource;

import junit.framework.*;
import junit.textui.TestRunner;
import com.sun.enterprise.ManagementObjectManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.management.util.J2EEManagementObjectManager;
import com.sun.enterprise.repository.IASJ2EEResourceFactoryImpl;
import com.sun.enterprise.repository.J2EEResource;
import java.util.logging.Logger;

/**
 * Unit test for ConnectorConnectionPoolDeployer.
 *
 * @author Rob Ruyak
 */
public class ConnectorConnectionPoolDeployerTest extends TestCase {

    ConnectorConnectionPoolDeployer deployer;
    ConnectorConnectionPool resource;

    /** Creates a new instance of MailResourceDeployerTest */
    public ConnectorConnectionPoolDeployerTest(String name) {
         super(name);
    }

    //TODO: Should ther be a unit test for null resources passed to the
    //deploy method???

    /**
     * Tests the deployResource method.
     *
    public void testDeployResourceWithNull() {
        try {
            deployer.deployResource(null);
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Exception caught -> " + e.getMessage());
        }
    }
     **/

    /**
     * Tests the deployResource method.
     */
    public void testDeployResource() {
        try {
            deployer.deployResource(resource);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception caught -> " + e.getMessage());
        }
    }

    /**
     * Tests the deployResource method.
     */
    public void testDeployResourceWithNull() {
        try {
            //This should print the following log:
            //INFO: CORE5005: Error in resource deploy.
            deployer.deployResource(null);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception caught -> " + e.getMessage());
        }
    }

    /**
     * Tests the deployResource method.
     */
    public void testDeployResourceNotEnabled() {
        try {
            //This should print the following log:
            //INFO: CORE5039: Resource named [jndi-name]
            //[Type: mail-resource] is disabled. It was not loaded.
            resource.setEnabled(false);
            deployer.deployResource(resource);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception caught -> " + e.getMessage());
        }
    }

    protected void setUp() {
        //initialize the dummy deployer object
        deployer = new ConnectorConnectionPoolDeployer () {
            Switch getAppServerSwitchObject() {
                return new Switch() {
                    public ManagementObjectManager getManagementObjectManager() {
                        return new J2EEManagementObjectManager() {
                            public void registerJMSResource(String name,
                                    String resType, String username,
                                    String password, String[] propNames,
                                    String[] propValues) {
                                System.out.println("Jsr77 Registration Complete...");
                            }
                        };
                    }
                };
            }
        };

        //initialize the dummy resource object
        resource = new ConnectorConnectionPool();
        resource.setName("jms/Tester");
        resource.setEnabled(true);
    }

    protected void tearDown() {
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(ConnectorConnectionPoolDeployerTest.class);
        return suite;
    }

     public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result =
                runner.doRun(ConnectorConnectionPoolDeployerTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}
