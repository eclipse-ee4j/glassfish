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

package com.sun.s1asdev.ejb.allowedmethods.ctxcheck.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.allowedmethods.ctxcheck.DriverHome;
import com.sun.s1asdev.ejb.allowedmethods.ctxcheck.Driver;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-allowedmethods-ctxcheck");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-allowedmethods-ctxcheck");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        localSlsbGetEJBObject();
        localSlsbGetEJBLocalObject();
        localSlsbGetEJBHome();
        localSlsbGetEJBLocalHome();
        localEntityGetEJBObject();
        localEntityGetEJBLocalObject();
        localEntityGetEJBHome();
        localEntityGetEJBLocalHome();
    }

    public void localSlsbGetEJBObject() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localSlsbGetEJBObject();
            stat.addStatus("ejbclient localSlsbGetEJBObject", stat.FAIL);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localSlsbGetEJBObject(-)" , stat.PASS);
        }
    }

    public void localSlsbGetEJBLocalObject() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localSlsbGetEJBLocalObject();
            stat.addStatus("ejbclient localSlsbGetEJBLocalObject", stat.PASS);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localSlsbGetEJBLocalObject" , stat.FAIL);
        }
    }

    public void localSlsbGetEJBLocalHome() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localSlsbGetEJBLocalHome();
            stat.addStatus("ejbclient localSlsbGetEJBLocalHome", stat.PASS);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localSlsbGetEJBLocalHome" , stat.FAIL);
        }
    }

    public void localSlsbGetEJBHome() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localSlsbGetEJBHome();
            stat.addStatus("ejbclient localSlsbGetEJBHome" , stat.FAIL);
        } catch(Exception e) {
            stat.addStatus("ejbclient localSlsbGetEJBHome(-)", stat.PASS);
        }
    }


    public void localEntityGetEJBObject() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localEntityGetEJBObject();
            stat.addStatus("ejbclient localEntityGetEJBObject", stat.FAIL);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
        }
    }

    public void localEntityGetEJBLocalObject() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localEntityGetEJBLocalObject();
            stat.addStatus("ejbclient localEntityGetEJBLocalObject", stat.PASS);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localEntityGetEJBLocalObject" , stat.FAIL);
        }
    }

    public void localEntityGetEJBLocalHome() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localEntityGetEJBLocalHome();
            stat.addStatus("ejbclient localEntityGetEJBLocalHome", stat.PASS);
        } catch(Exception e) {
            //e.printStackTrace();
            stat.addStatus("ejbclient localEntityGetEJBLocalHome" , stat.FAIL);
        }
    }

    public void localEntityGetEJBHome() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            driver.localEntityGetEJBHome();
            stat.addStatus("ejbclient localEntityGetEJBHome" , stat.FAIL);
        } catch(Exception e) {
            stat.addStatus("ejbclient localEntityGetEJBHome(-)", stat.PASS);
        }
    }

}

