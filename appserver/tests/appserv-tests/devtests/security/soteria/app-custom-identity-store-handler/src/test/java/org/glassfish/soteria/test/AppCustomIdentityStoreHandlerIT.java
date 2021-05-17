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

import static org.glassfish.soteria.test.Assert.assertAuthenticated;
import static org.glassfish.soteria.test.Assert.assertDefaultNotAuthenticated;
import static org.glassfish.soteria.test.Assert.assertNotAuthenticated;
import static org.glassfish.soteria.test.ShrinkWrap.mavenWar;

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
public class AppCustomIdentityStoreHandlerIT extends ArquillianBase {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    @Rule
    public TestWatcher reportWatcher=new ReportWatcher(stat, "Security::soteria::AppCustomIdentityStoreHandler");

    @AfterClass
    public static void printSummary(){
        stat.printSummary();
    }

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }

    @Test
    public void testAuthenticated() {
        assertAuthenticated(
            "web", "reza",
            readFromServer("/servlet?name=reza&password=secret1"),
            // Only groups from the
            "baz");
    }

    @Test
    public void testBlacklisted() {
        assertNotAuthenticated(
            "web", "rudy",
            readFromServer("/servlet?name=rudy&password=pw"),
            "foo", "bar");
    }

    @Test
    public void testNotAuthenticated() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
    }

    @Test
    public void testNotAuthenticatedWrongName() {
        assertNotAuthenticated(
            "web", "reza",
            readFromServer("/servlet?name=romo&password=secret1"),
            "foo", "bar", "baz");
    }

    @Test
    public void testNotAuthenticatedWrongPassword() {
        assertNotAuthenticated(
            "web", "reza",
            readFromServer("/servlet?name=reza&password=wrongpassword"),
            "foo", "bar", "baz");
    }

}
