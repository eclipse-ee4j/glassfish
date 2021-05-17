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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.glassfish.soteria.test.ArquillianBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import org.junit.Rule;
import org.junit.AfterClass;
import org.junit.rules.TestWatcher;

import com.gargoylesoftware.htmlunit.util.Cookie;

import static org.glassfish.soteria.test.Assert.assertDefaultAuthenticated;
import static org.glassfish.soteria.test.Assert.assertDefaultNotAuthenticated;
import static org.glassfish.soteria.test.ShrinkWrap.mavenWar;

@RunWith(Arquillian.class)
public class AppCustomRememberMeIT extends ArquillianBase {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    @Rule
    public TestWatcher reportWatcher=new ReportWatcher(stat, "Security::soteria::AppCustomRememberMe");

    @AfterClass
    public static void printSummary(){
      stat.printSummary();
    }

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }


    @Test
    public void testHttpOnlyIsTrue() {
        readFromServer("/servlet?name=reza&password=secret1&rememberme=true");

        assertTrue(getWebClient().getCookieManager().getCookie("GLASSFISHCOOKIE").isHttpOnly());
    }

    @Test
    public void testSecureOnlyIsFalse() {
        readFromServer("/servlet?name=reza&password=secret1&rememberme=true");

        assertFalse(getWebClient().getCookieManager().getCookie("GLASSFISHCOOKIE").isSecure());
    }

    @Test
    public void testAuthenticated() {
        assertDefaultAuthenticated(
            readFromServer("/servlet?name=reza&password=secret1"));
    }

    @Test
    public void testNotAuthenticated() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
    }

    @Test
    public void testNotAuthenticatedWrongName() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet?name=romo&password=secret1"));
    }

    @Test
    public void testNotAuthenticatedWrongPassword() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet?name=reza&password=wrongpassword"));
    }

    @Test
    public void testAuthenticatedRememberMe() {

        // 1. Initially request page when we're not authenticated

        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));


        // 2. Authenticate without remember me

        String response = readFromServer("/servlet?name=reza&password=secret1");

        assertDefaultAuthenticated(
            response);

        // For the initial authentication, the mechanism should be called

        assertTrue(
            "Authentication mechanism should have been called, but wasn't",
            response.contains("authentication mechanism called: true"));


        // 3. Request same page again within same http session, without remember me
        //    specified should NOT be authenticated

        response = readFromServer("/servlet");

        assertDefaultNotAuthenticated(
            response);


        // 4. Authenticate with remember me

        response = readFromServer("/servlet?name=reza&password=secret1&rememberme=true");

        assertDefaultAuthenticated(
            response);

        // For the initial authentication, the mechanism should be called again

        assertTrue(
            "Authentication mechanism should have been called, but wasn't",
            response.contains("authentication mechanism called: true"));


        // 5. Request same page again within same http session, with remember me
        //    specified should be authenticated

        response = readFromServer("/servlet");

        assertDefaultAuthenticated(
            response);

        // For the subsequent authentication, the mechanism should not be called again
        // (the remember me interceptor takes care of this)

        assertTrue(
            "Authentication mechanism should not have been called, but was",
            response.contains("authentication mechanism called: false"));


        // 6. "Expire" the session by removing all cookies except the
        //    remember me cookie

        for (Cookie cookie : getWebClient().getCookieManager().getCookies()) {
            if (!"GLASSFISHCOOKIE".equals(cookie.getName())) {
                getWebClient().getCookieManager().removeCookie(cookie);
            }
        }

        // Request same page again

        response = readFromServer("/servlet");

        // Should still be authenticated

        assertDefaultAuthenticated(
            response);

        // For the subsequent authentication, the mechanism should not be called again
        // (the remember me interceptor takes care of this)

        assertTrue(
            "Authentication mechanism should not have been called, but was",
            response.contains("authentication mechanism called: false"));


        // 7. Logout. Should not be authenticated anymore

        assertDefaultNotAuthenticated(
            readFromServer("/servlet?logout=true"));


        // 8. Request same page again, should still not be authenticated

        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));

    }

}
