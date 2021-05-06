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

package com.acme;

import java.util.Map;
import java.util.HashMap;
import jakarta.ejb.*;
import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String[] s) {
        appName = s[0];
        stat.addDescription(appName);
        Client t = new Client();
        t.test();
        stat.printSummary(appName + "ID");

    }

    private void test() {

        //Map<String, Object> p = new HashMap<String, Object>();
        //p.put(EJBContainer.MODULES, "sample");

        EJBContainer c = EJBContainer.createEJBContainer();
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.out.println("Sleeping for a second...");
            Thread.sleep(1000);

            SingletonBean sb = (SingletonBean) ic.
                lookup("java:global/classes/SingletonBean!com.acme.SingletonBean");
            sb.hello();

            StatefulBean sfTimeout = (StatefulBean) ic.
                lookup("java:global/classes/StatefulBean");
            StatefulBean2 sfNoTimeout = (StatefulBean2) ic.
                lookup("java:global/classes/StatefulBean2");
            sfTimeout.hello();
            sfNoTimeout.hello();

            System.out.println("Sleeping to wait for sf bean to be removed ...");
            Thread.sleep(7000);
            System.out.println("Waking up , checking sf bean existence");

            try {
                sfTimeout.hello();
                throw new RuntimeException("StatefulTimeout(0) bean should have timed out");
            } catch(EJBException e) {
                System.out.println("Stateful bean successfully timed out");
            }

            sfNoTimeout.hello();
            System.out.println("Stateful bean with longer timeout is still around");

            /**
            HelloRemote hr = (HelloRemote) ic.
                lookup("java:global/classes/SingletonBean!com.acme.HelloRemote");
            hr.hello();
            */

            if( sb.getPassed() ) {
                System.out.println("getPassed() returned true");
                stat.addStatus("embedded async test", stat.PASS);
            } else {
                throw new EJBException("getPassed() returned false");
            }
        } catch (Exception e) {
            stat.addStatus("embedded async test", stat.FAIL);
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        }

        System.out.println("Closing container");

        c.close();
        System.out.println("Done Closing container");

    }

}
