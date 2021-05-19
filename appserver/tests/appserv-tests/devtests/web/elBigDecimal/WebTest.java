/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.el.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for coverting long to BigDecimal in EL
 * issue 7479
 */

public class WebTest {

    private static final String testName = "EL-long-to-BigDecimal";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        stat.addDescription("Unit tests for logn to BigDecimal coersion");

        ExpressionFactory ef = ExpressionFactory.newInstance();
        Object o = ef.coerceToType(new Long("1234567890123456789"),
                                   java.math.BigDecimal.class);
        System.out.println("Result: " + o);
        if ("1234567890123456789".equals(o.toString()))
            stat.addStatus(testName, stat.PASS);
        else
            stat.addStatus(testName, stat.FAIL);

        stat.printSummary();
    }
}

