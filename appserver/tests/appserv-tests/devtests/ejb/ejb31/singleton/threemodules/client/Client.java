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

package com.sun.devtest.client;

//import com.sun.mod1.SingletonBean;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import jakarta.ejb.*;
import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import org.glassfish.devtest.ejb31.singleton.servlet.RemoteInitTracker;

public class Client {


        private static String[] EXPECTED_ORDER = new String[] {
                "org.glassfish.devtest.ejb31.singleton.servlet.InitOrderTrackerBean",
                "org.glassfish.devtest.ejb31.singleton.mod1.BeanA_Mod1",
                "org.glassfish.devtest.ejb31.singleton.mod1.RootBean_Mod1",
                "org.glassfish.devtest.ejb31.singleton.mod2.BeanA_Mod2",
                "org.glassfish.devtest.ejb31.singleton.mod2.RootBean_Mod2"
        };

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    RemoteInitTracker tracker;

    public static void main(String[] args) {
        appName = args[0];
        stat.addDescription(appName);
        Client t = new Client();
        t.test();
        stat.printSummary(appName + "ID");
    }

    private void test() {
        try {

                String lookupName = "java:global/ejb-ejb31-singleton-threemodulesApp/ejb-ejb31-singleton-threemodules-ejb/InitOrderTrackerBean!org.glassfish.devtest.ejb31.singleton.servlet.RemoteInitTracker";
                System.out.println("*****************************************************");
                System.out.println("*** " + lookupName + " ***");
                System.out.println("*****************************************************");
                tracker = (RemoteInitTracker) new InitialContext().lookup(lookupName);

                List<String> actualList = tracker.getInitializedNames();
                boolean result = actualList.size() == EXPECTED_ORDER.length;
                for (int i=0; i<EXPECTED_ORDER.length; i++) {
                        if (! EXPECTED_ORDER[i].equals(actualList.get(i))) {
                                result = false;
                        }
                }
System.out.println(actualList);
                stat.addStatus("EJB singleton-three-module-dependency",
                        (result ? stat.PASS : stat.FAIL));
        } catch (Throwable th) {
                th.printStackTrace();
                stat.addStatus("EJB singleton-three-module-dependency", stat.FAIL);
        }
    }

}
