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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import org.junit.Rule;
import org.junit.AfterClass;
import org.junit.rules.TestWatcher;

import static org.glassfish.soteria.test.Assert.assertDefaultAuthenticated;
import static org.glassfish.soteria.test.Assert.assertDefaultNotAuthenticated;
import static org.glassfish.soteria.test.ShrinkWrap.mavenWar;

@RunWith(Arquillian.class)
public class AppMultipleStoreIT extends ArquillianBase {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    @Rule
    public TestWatcher reportWatcher=new ReportWatcher(stat, "Security::soteria::AppMultipleStore");

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

}
