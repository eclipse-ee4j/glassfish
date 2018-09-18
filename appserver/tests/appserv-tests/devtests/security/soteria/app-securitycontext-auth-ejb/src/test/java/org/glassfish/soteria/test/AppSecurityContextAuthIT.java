/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.soteria.test;

import static org.glassfish.soteria.test.Assert.assertDefaultAuthenticated;
import static org.glassfish.soteria.test.Assert.assertDefaultNotAuthenticated;
import static org.glassfish.soteria.test.ShrinkWrap.mavenWar;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import org.junit.Rule;
import org.junit.AfterClass;
import org.junit.rules.TestWatcher;


@RunWith(Arquillian.class)
public class AppSecurityContextAuthIT extends ArquillianBase {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    @Rule
    public TestWatcher reportWatcher=new ReportWatcher(stat, "Security::soteria::AppSecurityContextEJB");

    @AfterClass
    public static void printSummary(){
        stat.printSummary();
    }
    
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }
    
    @Test
    public void testAuthenticatedStatus() {
        assertTrue(
            readFromServer("/servlet?name=reza")
                .contains("Authenticated with status: SUCCESS"));
    }
    
    /**
     * The name "rezax" will cause the custom authentication provider
     * to throw an auth exception, which should ultimately result in
     * a SEND_FAILURE outcome from SecurityContext.authenticate.
     */
    @Test
    public void testAuthenticatedStatusException() {
        assertTrue(
            readFromServer("/servlet?name=rezax")
                .contains("Authenticated with status: SEND_FAILURE"));
    }
    
    /**
     * The name "unknown" will cause the custom authentication provider
     * to return SEND_FAILURE, which should ultimately result in
     * a SEND_FAILURE outcome from SecurityContext.authenticate as well.
     */
    @Test
    public void testAuthenticatedStatusFail() {
        assertTrue(
            readFromServer("/servlet?name=unknown")
                .contains("Authenticated with status: SEND_FAILURE"));
    }

    @Test
    public void testAuthenticated() {
        Assert.assertAuthenticated(
                "ejb",
                "reza",
                readFromServer("/servlet?name=reza"));
    }

    @Test
    public void testContextAuthenticated() {
        Assert.assertAuthenticated(
            "context",
            "reza",
            readFromServer("/servlet?name=reza"));
    }

    @Test
    public void testContextIsCallerInRole(){
        Assert.assertAuthenticated(
                "context",
                "reza",
                readFromServer("/servlet?name=reza"), "foo", "bar");
    }

    @Test
    public void testContextAllCallers(){
        Assert.assertAuthenticatedRoles(
                "all roles",
                readFromServer("/servlet?name=reza"), "foo", "bar");
    }

    @Test
    public void testContextHasAccessToResource(){
        Assert.assertHasAccessToResource(
                "web",
                "reza",
                "/protectedServlet",
                readFromServer("/servlet?name=reza"));
    }
    
    @Test
    public void testNotAuthenticated() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
    }
    
  
    

}
