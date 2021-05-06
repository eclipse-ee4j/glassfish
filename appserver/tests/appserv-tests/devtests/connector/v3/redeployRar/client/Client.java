/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package client;

import beans.*;
import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client   {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        Client client = new Client();
        System.out.println("Client: Args: ");
        for(int i = 0 ; i< args.length; i++) {
                System.out.println("Client: Args: " + args[i]);
        }
        Integer versionNumber = new Integer(args[0]);
        client.doTest(versionNumber.intValue());
    }

    public String doTest(int versionToTest) {
        stat.addDescription("This is to test redeployment of connector modules. Testing version " + versionToTest);

        String res = "NOT RUN";
        debug("doTest() ENTER...");
        boolean pass = false;
        try {
                pass = checkResults(versionToTest);
                debug("Got expected results = " + pass);

                //do not continue if one test failed
                if (!pass) {
                        res = "SOME TESTS FAILED";
                        stat.addStatus("Redeploy Connector 1.5 test - Version : "+ versionToTest, stat.FAIL);
                } else {
                        res  = "ALL TESTS PASSED";
                        stat.addStatus("Redeploy Connector 1.5 test - Version : " + versionToTest , stat.PASS);
                }
        } catch (Exception ex) {
            System.out.println("Redeploy connector test failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
        }
        stat.printSummary("Redeploy Connector 1.5");

        debug("EXITING... STATUS = " + res);
        return res;
    }

    private boolean checkResults(int num) throws Exception {
            debug("checkResult" + num);
            debug("got initial context" + (new InitialContext()).toString());
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/MyVersionChecker");
        debug("got o" + o);
        VersionCheckerHome  home = (VersionCheckerHome)
            PortableRemoteObject.narrow(o, VersionCheckerHome.class);
        debug("got home" + home);
            VersionChecker checker = home.create();
            debug("got o" + checker);
        //problem here!
        int result = checker.getVersion();
        debug("checkResult" + result);
        return result == num;
    }

    private void debug(String msg) {
        System.out.println("[Redeploy Connector CLIENT]:: --> " + msg);
    }
}

