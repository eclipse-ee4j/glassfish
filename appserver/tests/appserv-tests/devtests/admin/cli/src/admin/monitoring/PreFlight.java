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

import static admin.monitoring.Constants.*;

/**
 * Enforce PreFlight Assumptions
 * This should never fail on a Hudson build and can easily fail for a developer
 * environment (e.g. Oops I left a GlassFish server running on port 28080!)
 * @author Byron Nevins
 */
public class PreFlight extends MonTest {
    @Override
    void runTests(TestDriver driver) {
                new String();



        setDriver(driver);
        report(true, "PreFlight here!!!");
        boolean b1 = wget(8080, "");
        boolean b2 = wget(28080, "");
        boolean b3 = wget(28081, "");
        report(!b1, "Port 8080 Clear");
        report(!b2, "Port 28080 Clear");
        report(!b3, "Port 28081 Clear");
        // todo check that DB is **not** running

        if (b1 || b2 || b3) {
            report(false, "Monitoring Pre-Flight Failed::Aborted All Tests");
            System.out.println(SCREAMING_LOUD_MESSAGE);
            throw new RuntimeException("PreFlight");
        }
    }
    private static final String SCREAMING_LOUD_MESSAGE =
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "*****FATAL ERROR -- ABORTING MONITORING TESTS !!!!!***\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n" +
            "******************************************************\n";
}
