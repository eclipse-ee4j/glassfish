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

package simplehandler;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.rpc.Service;
import jakarta.xml.soap.SOAPElement;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import servlet.*;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        stat.addDescription("webservices-simple-soapfault");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-soapfaultID");
    }

    public void doTest(String[] args) {
            try {

            String targetEndpointAddress = args[0];

            Context ic = new InitialContext();

            Service testService = (Service) ic.lookup("java:comp/env/service/simplehandler");
            SimpleServer test = (SimpleServer)
            testService.getPort(SimpleServer.class);

            ((Stub)test)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                    targetEndpointAddress);

            System.out.println("endpoint address = " + targetEndpointAddress);
            System.out.println("Invocation returned " + test.sayHello("jerome"));
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("soapfaultsclient main", stat.FAIL);
            System.out.println("CAUGHT UNEXPECTED EXCEPTION: " + ex.getMessage());
        }

        stat.addStatus("soapfaultsclient main", stat.PASS);
    }
}
