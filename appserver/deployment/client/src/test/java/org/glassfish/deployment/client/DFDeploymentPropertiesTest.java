/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim
 */
public class DFDeploymentPropertiesTest {

    public DFDeploymentPropertiesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setProperties method, of class DFDeploymentProperties.
     */
    @Test
    public void testSetProperties() {
        Properties props = new Properties();
        props.setProperty("keepSessions", "true");
        props.setProperty("foo", "bar");

        DFDeploymentProperties instance = new DFDeploymentProperties();
        instance.setProperties(props);

        String storedProps = (String) instance.get(DFDeploymentProperties.PROPERTY);
        assertEquals(storedProps, "keepSessions=true:foo=bar");
    }

    /**
     * Test of getProperties method, of class DFDeploymentProperties.
     */
    @Test
    public void testGetProperties() {
        DFDeploymentProperties instance = new DFDeploymentProperties();
        instance.put(DFDeploymentProperties.PROPERTY, "keepSessions=true:foo=bar");
        Properties expResult = new Properties();
        expResult.setProperty("keepSessions", "true");
        expResult.setProperty("foo", "bar");

        Properties result = instance.getProperties();
        assertEquals(expResult, result);
    }


}
