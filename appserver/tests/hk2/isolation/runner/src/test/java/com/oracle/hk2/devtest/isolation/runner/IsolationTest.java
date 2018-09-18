/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest.isolation.runner;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.glassfish.tests.utils.NucleusStartStopTest;
import org.glassfish.tests.utils.NucleusTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Ensures that different apps get different service locators
 * 
 * @author jwells
 *
 */
public class IsolationTest extends NucleusStartStopTest {
    private final static String ISO1_WAR = "isolation/web/iso1/target/hk2-isolation-web-iso1.war";
    private final static String ISO1_APP_NAME = "hk2-isolation-web-iso1";
    private final static String ISO1_URL = "http://localhost:8080/hk2-isolation-web-iso1/iso1";
    
    private final static String ISO2_WAR = "isolation/web/iso2/target/hk2-isolation-web-iso2.war";
    private final static String ISO2_APP_NAME = "hk2-isolation-web-iso2";
    private final static String ISO2_URL = "http://localhost:8080/hk2-isolation-web-iso2/iso2";
    
    private final static String SOURCE_HOME = System.getProperty("source.home", "$");
    private final static String SOURCE_HOME_ISO1_WAR = "/appserver/tests/hk2/" + ISO1_WAR;
    private final static String SOURCE_HOME_ISO2_WAR = "/appserver/tests/hk2/" + ISO2_WAR;
    
    private static final String SERVLET_CONTEXT_LOCATOR = "ServletContextLocator";
    private static final String JNDI_APP_LOCATOR = "JndiAppLocator";
    
    private boolean deployed1;
    private boolean deployed2;
    
    private Map<String, String> getNames(String rawHTML) {
        Map<String, String> retVal = new HashMap<String, String>();
        
        StringTokenizer st = new StringTokenizer(rawHTML, "\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            
            int equalsIndex = line.indexOf('=');
            if (equalsIndex < 0) continue;  // Skip lines that do not have = in it
            
            String key = line.substring(0, equalsIndex);
            String value = line.substring(equalsIndex + 1, line.length());
            
            retVal.put(key, value);
        }
        
        return retVal;
    }
    
    private String getName(String rawHTML, String key) {
        Map<String, String> names = getNames(rawHTML);
        
        return names.get(key);
    }
    
    @BeforeTest
    public void beforeTest() {
        String iso1War = ISO1_WAR;
        String iso2War = ISO2_WAR;
        
        if (!SOURCE_HOME.startsWith("$")) {
            iso1War = SOURCE_HOME + SOURCE_HOME_ISO1_WAR;
            iso2War = SOURCE_HOME + SOURCE_HOME_ISO2_WAR;
        }
        
        deployed1 = NucleusTestUtils.nadmin("deploy", iso1War);
        deployed2 = NucleusTestUtils.nadmin("deploy", iso2War);
        
        Assert.assertTrue(deployed1);
        Assert.assertTrue(deployed2);
    }
    
    @AfterTest
    public void afterTest() {
        if (deployed1) {
            deployed1 = false;
            NucleusTestUtils.nadmin("undeploy", ISO1_APP_NAME);
        }
        if (deployed2) {
            deployed2 = false;
            NucleusTestUtils.nadmin("undeploy", ISO2_APP_NAME);
        }
    }
    
    /**
     * Ensures that the service locators in two web-apps are different
     */
    @Test(enabled=false)
    public void testWebAppsAreIsolated() {
        String fromURL1 = NucleusTestUtils.getURL(ISO1_URL);
        String fromURL2 = NucleusTestUtils.getURL(ISO2_URL);
            
        String iso1Name = getName(fromURL1, SERVLET_CONTEXT_LOCATOR);
        String iso2Name = getName(fromURL2, SERVLET_CONTEXT_LOCATOR);
            
        Assert.assertNotEquals(iso1Name, iso2Name);
    }
    
    /**
     * Ensures that the application service locators in two web-apps are different
     */
    @Test
    public void testWebAppsApplicationServiceLocatorsAreIsolated() {
        String fromURL1 = NucleusTestUtils.getURL(ISO1_URL);
        String fromURL2 = NucleusTestUtils.getURL(ISO2_URL);
            
        String iso1Name = getName(fromURL1, JNDI_APP_LOCATOR);
        String iso2Name = getName(fromURL2, JNDI_APP_LOCATOR);
            
        Assert.assertNotEquals(iso1Name, iso2Name);
    }
}
