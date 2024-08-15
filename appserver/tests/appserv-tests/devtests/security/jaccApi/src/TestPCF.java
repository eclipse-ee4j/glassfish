/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package jakarta.security.jacc;

import jakarta.security.jacc.PolicyConfigurationFactory;
import jakarta.security.jacc.PolicyConfiguration;
import jakarta.security.jacc.PolicyContextException;
import java.security.AccessControlException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class TestPCF {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::JACC API testPCF ";

    public static void main ( String[] args ) {
        stat.addDescription(testSuite);
        String description = null;
        boolean expectACException =
            (args != null && args.length > 0) ?
            Boolean.parseBoolean(args[0]) : true;
        String expectedException =
            (args != null && args.length > 1) ? args[1] : null;
        System.out.println("expect AccessControlException: " + expectACException);
        System.out.println("expected Exception: " + expectedException);

        description = testSuite + "-" + expectACException + "-" + expectedException;
        try {
            PolicyConfigurationFactory f =
                PolicyConfigurationFactory.getPolicyConfigurationFactory();
            stat.addStatus(description, stat.PASS);
        } catch(Exception ex) {
            //It should be one of the following:
            //    java.lang.ClassNotFoundException
            //    java.lang.ClassCastException
            //    jakarta.security.jacc.PolicyContextException
            if (ex.getClass().getName().equals(expectedException)) {
                stat.addStatus(description, stat.PASS);
            } else {
                ex.printStackTrace();
                stat.addStatus(description, stat.FAIL);
            }
        }

        stat.printSummary(testSuite);
    }
}










