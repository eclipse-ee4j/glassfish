/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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


import java.io.*;
import java.util.*;
import jakarta.ejb.EJB;
import javax.naming.InitialContext;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("simple-ejb-implicit-cdi-deployment-opt");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("simple-ejb-implicit-cdi-deployment-opt");
    }

    public Client (String[] args) {
    }

    private static @EJB(mappedName="Sless") Sless sless;

    public void doTest() {

        try {

            System.out.println("Creating InitialContext()");
            InitialContext ic = new InitialContext();
            org.omg.CORBA.ORB orb = (org.omg.CORBA.ORB) ic.lookup("java:comp/ORB");
            Sless sless = (Sless) ic.lookup("Sless");

            String response = null;

            response = sless.hello();
            testResponse("invoking stateless", response);

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

    	return;
    }

    private void testResponse(String testDescription, String response){
        // Expecting a null response because the injection should fail since implicit bean discovery
        // is disabled by the deployment property implicitCdiEnabled=false
        stat.addStatus(testDescription, (response == null ? stat.PASS : stat.FAIL));
    }

}

