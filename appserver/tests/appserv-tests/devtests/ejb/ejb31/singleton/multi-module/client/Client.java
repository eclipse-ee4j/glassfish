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
import jakarta.ejb.*;
import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import org.glassfish.devtest.ejb31.singleton.multimodule.servlet.RemoteInitTracker;

public class Client {

        private static String[] BEAN_NAMES = new String[] {
                "org.glassfish.devtest.ejb31.singleton.multimodule.servlet.InitOrderTrackerBean",
                "org.glassfish.devtest.ejb31.singleton.multimodule.mod1.BeanA_Mod1",
                "org.glassfish.devtest.ejb31.singleton.multimodule.mod1.RootBean_Mod1",
                "org.glassfish.devtest.ejb31.singleton.multimodule.mod2.BeanA_Mod2",
                "org.glassfish.devtest.ejb31.singleton.multimodule.mod2.RootBean_Mod2"
        };

        private static final String INIT_ORDER_BEAN = BEAN_NAMES[0];

        private static final String BEAN_MOD1 = BEAN_NAMES[1];

        private static final String ROOT1 = BEAN_NAMES[2];

        private static final String BEAN_MOD2 = BEAN_NAMES[3];

        private static final String ROOT2 = BEAN_NAMES[4];

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

                String lookupName = "java:global/ejb-ejb31-singleton-multimoduleApp/ejb-ejb31-singleton-multimodule-ejb/InitOrderTrackerBean!org.glassfish.devtest.ejb31.singleton.multimodule.servlet.RemoteInitTracker";
                System.out.println("*****************************************************");
                System.out.println("*** " + lookupName + " ***");
                System.out.println("*****************************************************");
                tracker = (RemoteInitTracker) new InitialContext().lookup(lookupName);

                Map<String, Integer> initOrder = tracker.getInitializedNames();
                boolean result = initOrder.size() == BEAN_NAMES.length;

                int b1m1 = initOrder.get(BEAN_MOD1);
                int b2m2 = initOrder.get(BEAN_MOD2);
                int root1 = initOrder.get(ROOT1);
                int root2 = initOrder.get(ROOT2);

                boolean test1 = b1m1 < root1;
                boolean test2 = root1 < root2;
                boolean test3 = b2m2 < root2;
                for (String key : initOrder.keySet()) {
                        System.out.println(key + ": " + initOrder.get(key));
                }

                result = result && test1 && test2 && test3;

                stat.addStatus("EJB singleton-cross-module-dependency",
                        (result ? stat.PASS : stat.FAIL));
        } catch (Throwable th) {
                th.printStackTrace();
                stat.addStatus("EJB singleton-cross-module-dependency", stat.FAIL);
        }
    }

}
