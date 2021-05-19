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

package soapfaults;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.xml.rpc.Stub;
import jakarta.xml.soap.SOAPElement;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        stat.addDescription("webservices-soapfaults");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-soapfaultsID");
    }

    public void doTest(String[] args) {
            try {

           String targetEndpointAddress = args[0];

            Context ic = new InitialContext();

            TestService testService =
                (TestService) ic.lookup("java:comp/env/service/soapfaults");
            Test test = testService.getTestPort();

            ((Stub)test)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                                            targetEndpointAddress);

            System.out.println("endpoint address = " + targetEndpointAddress);

            Test2RequestType c = new Test2RequestType("recess", "weekender");

            try {
              Test1ResponseType ret = test.test1("1", "test fault one", c);
            } catch (FaultOne ex) {
              System.out.println("CAUGHT EXPECTED EXCEPTION: FaultOne: " + ex.getMessage1());
            }

            try {
              Test1ResponseType ret = test.test1("2", "test fault two", c);
            } catch (FaultTwo ex) {
              System.out.println("CAUGHT EXPECTED EXCEPTION: FaultTwo: " + ex.getMessage2());
            }

            try {
              Test1ResponseType ret = test.test1("3", "test fault three", c);
            } catch (FaultThree ex) {
              System.out.println("CAUGHT EXPECTED EXCEPTION: FaultThree: " + ex.getMessage3());
            }

            stat.addStatus("soapfaultsclient main", stat.PASS);
            } catch (Exception ex) {
            System.out.println("soapfaults client test failed");
            ex.printStackTrace();
            stat.addStatus("soapfaultsclient main", stat.FAIL);
            //System.exit(15);
        }
    }
}
