/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

public class BeanSetup {
    public static boolean disposeCalledForPreferred = false;
    public static String disposeCalledForTestQualifierError = "";

    @RequestScoped
    @Produces
    @Preferred
    public TestRequestScopedBean get(TestRequestScopedBean trs) {
        return trs;
    }

    void dispose(@Disposes @Preferred TestRequestScopedBean trs) {
        System.out
                .println("Dispose method in BeanSetup for Preferred called");
        disposeCalledForPreferred = true;
    }

    @RequestScoped
    @Produces
    @TestQualifier
    public TestRequestScopedBean getTestQualifier(TestRequestScopedBean trs) {
        return trs;
    }

    //test injection of TestBean in this disposer method
    void disposeTestQualifier(
            @Disposes @TestQualifier TestRequestScopedBean trs, TestBean tb) {
        System.out
                .println("Dispose method in BeanSetup for testQualifier called");
        if (tb != null) {
            disposeCalledForTestQualifierError = "dispose method for testqualifier " +
                    "called without injecting TestBean";
        }
    }

}
