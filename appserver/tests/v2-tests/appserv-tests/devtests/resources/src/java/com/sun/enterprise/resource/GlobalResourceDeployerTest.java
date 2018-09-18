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

import com.sun.enterprise.config.serverbeans.ElementProperty;
import java.util.Properties;
import junit.framework.*;
import junit.textui.TestRunner;

/**
 * Unit test for ConnectorConnectionPoolDeployer.
 *
 * @author Rob Ruyak
 */
public class GlobalResourceDeployerTest extends TestCase {

    MailResourceDeployer deployer;
    ElementProperty [] testProps;
    String [] testNames;
    String [] testValues;
    
    /** Creates a new instance of ConnectorConnectionPoolDeployerTest */
    public GlobalResourceDeployerTest(String name) {
         super(name);
    }

    /**
     * Tests the getPropNamesAsStrArr method.
     *
     */
    public void testGetPropNamesAsStrArr() {
        String [] result = deployer.getPropNamesAsStrArr(testProps);
        assertNotNull(result);
        assertEquals(result[0], testNames[0]);
        assertEquals(result[1], testNames[1]);
        assertEquals(result[2], testNames[2]);
    }
    
    /**
     * Tests the getPropNamesAsStrArr method with null param.
     *
     */
    public void testGetPropNamesAsStrArrWithNull() {
        String [] result = deployer.getPropNamesAsStrArr(null);
        assertNull(result);
    }
    
    /**
     * Tests the getPropValuesAsStrArr method.
     *
     */
    public void testGetPropValuesAsStrArr() {
        String [] result = deployer.getPropValuesAsStrArr(testProps);
        assertNotNull(result);
        assertEquals(result[0], testValues[0]);
        assertEquals(result[1], testValues[1]);
        assertEquals(result[2], testValues[2]);   
    }
    
    /**
     * Tests the getPropValuesAsStrArr method with null param.
     */
    public void testGetPropValuesAsStrArrWithNull() {
        String [] result = deployer.getPropValuesAsStrArr(null);
        assertNull(result);
    }
    
    protected void setUp() {
       deployer = new MailResourceDeployer();
       ElementProperty prop1 = new ElementProperty();
       prop1.setName("user");
       prop1.setValue("admin");
       ElementProperty prop2 = new ElementProperty();
       prop2.setName("password");
       prop2.setValue("adminadmin");
       ElementProperty prop3 = new ElementProperty();
       prop3.setName("status");
       prop3.setValue("enabled");
       testProps = new ElementProperty[] {prop1, prop2, prop3};
       testNames = new String[] {"user","password","status"};
       testValues = new String[] {"admin","adminadmin","enabled"};
    }

    protected void tearDown() {
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(GlobalResourceDeployerTest.class);
        return suite;
    }
    
     public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result =
                runner.doRun(GlobalResourceDeployerTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}
