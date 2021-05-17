/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.ee.synchronization.cleaner;

import java.io.File;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Synchronization cleaner test.
 *
 * @author Nazrul Islam
 */
public class SynchronizationCleanerTest extends TestCase {

    public SynchronizationCleanerTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    /**
     * Tests the remove functionality of file handler.
     */
    public void test() {

        try {
            String iRoot = System.getProperty("com.sun.aas.instanceRoot");
            String appname = System.getProperty("appname");
            String appdir = iRoot + File.separator + "applications"
                + File.separator + "j2ee-apps" + File.separator + appname;

            System.out.println("Application dir: " + appdir);

            File app = new File(appdir);

            // file should be removed
            assertTrue( !app.exists() );
            System.out.println("Application dir is removed after restart.");

            String gendir = iRoot + File.separator + "generated"
              + "ejb" + File.separator + "j2ee-apps" + File.separator + appname;
            File gen = new File(gendir);
            assertTrue( !gen.exists() );
            System.out.println("Generated dir is removed after restart.");

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    /*
    public static TestSuite suite() {
        //To run all tests
        return new TestSuite(SynchronizationMementoTest.class);
        //To run a subset of the tests
        TestSuite suite = new TestSuite();
        suite.addTest(new SynchronizationMementoTest("testEmptySync"));
        suite.addTest(new SynchronizationMementoTest("testEmptySync2"));
        return suite;
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(SynchronizationCleanerTest.suite());
    }
    */

    public static void main(String args[]) {
        junit.textui.TestRunner.run(SynchronizationCleanerTest.class);
    }
}
