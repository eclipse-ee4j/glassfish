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

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class TestClient {
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) throws Exception {
        try {
            stat.addDescription("Testing bmp simple_nopackage app.");
            InitialContext ic = new InitialContext();
            TestHome home = (TestHome) javax.rmi.PortableRemoteObject.narrow(ic.lookup("ejb/Test"), TestHome.class);
            System.out.println("Starting test");
            Test test = home.create(1);
            test.foo();
            System.out.println("Done");
            stat.addStatus("bmp simple_nopackage", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("bmp simple_nopackage", stat.FAIL);
        }
        stat.printSummary("simple_nopackage");
    }
}
