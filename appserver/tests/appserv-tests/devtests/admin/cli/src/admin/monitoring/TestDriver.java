/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package admin.monitoring;

import admin.AdminBaseDevTest;

/**
 * This is the Lord of the Monitoring Tests -- the Class of Power to drive them all.
 * Keep an eye out for Gollum.
 * @author Byron Nevins
 */
public final class TestDriver extends AdminBaseDevTest {
    public TestDriver() {
    }

    @Override
    protected String getTestDescription() {
        return "DevTests for Monitoring - Brought to you by\n"
                + "Jennifer Chou and Byron Nevins";
    }

    @Override
    public String getTestName() {
        return "Monitoring DevTests";
    }

    public static void main(String[] args) {
        // top level try here!!
        TestDriver driver = new TestDriver();
        System.out.printf("HADAS sysprop=%s, env var=%s, hadas sys prop=%s, env var=%s\n",
                System.getProperty("HADAS"),
                System.getenv("HADAS"),
                System.getProperty("hadas"),
                System.getenv("hadas")
                );
        try {
            driver.runTests();
        }
        catch (Exception e) {
            driver.report("GotException-" + e.getClass().getName(), false);
            e.printStackTrace();
        }
        driver.stat.printSummary();
    }

    private void runTests() {
        report("TestDriver Creation", true);

        for (MonTest mt : tests) {
            mt.runTests(this);
        }

    }
     private MonTest tests[] = new MonTest[]{
        new PreFlight(),
        new Setup(),
        new EarlyJira(), // these tests want monitoring disabled...
        new Enabler(),
        new Ejb(),
        new Jdbc(),
        new Jira(),
        new Web(),
        new TearDown(),
    };
}
