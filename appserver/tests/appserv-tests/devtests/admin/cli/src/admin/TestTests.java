/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package admin;

import java.io.*;
import java.net.*;
/**
 * This class is here just for doing testing on the tests.  Normally it doesn't run
 *
 * @author wnevins
 */
public class TestTests extends AdminBaseDevTest {

    public TestTests() {
    }

    public static void main(String[] args) {
        // this calls baseclass which calls our subrun() method
        new TestTests().run();
    }

    @Override
    public String getTestName() {
        return "Testing of the Development Tests Themselves";
    }

    @Override
    protected String getTestDescription() {
        return "Tests for the Testing Framework";
    }

    @Override
    public void subrun() {
        File f = new File(".");
        System.out.println("CWD == " + f.getAbsolutePath());
        report("version", asadmin("version"));
        report("version", asadmin("version"));
        report("version", asadmin("version"));
        report("version", asadmin("version"));
        //report("fake", false);
    }
}
