/*
 * Copyright (c) 2008, 2021 Oracle and/or its affiliates. All rights reserved.
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
import test.*;
import java.io.*;
import java.util.*;
import jakarta.ejb.EJB;
import javax.naming.InitialContext;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        stat.addDescription("simple-ejb-implicit-cdi");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("simple-ejb-implicit-cdi");
    }

    public Client(String[] args) {
    }

    @EJB(mappedName = "test.Foo#test.Foo")
    private static Foo sless;

    //
    // NOTE: Token 3700 will be replaced in @EJB annotations below
    // with the value of the port from config.properties during the build
    //
    @EJB(mappedName = "corbaname:iiop:localhost:3700#test.Foo") 
    private static Foo sless2;

    @EJB(mappedName = "corbaname:iiop:localhost:3700#java:global/simple-ejb-implicit-cdiApp/simple-ejb-implicit-cdi-ejb/SlessEJB!test.Foo") 
    private static Foo sless3;

    public void doTest() {

        try {
            System.out.println("Creating InitialContext()");
            InitialContext initialContext = new InitialContext();
            org.omg.CORBA.ORB orb = (org.omg.CORBA.ORB) initialContext.lookup("java:comp/ORB");
            Foo sless = (Foo) initialContext.lookup("test.Foo#test.Foo");

            String response = sless.hello();
            testResponse("invoking stateless", response);

            response = sless2.hello();
            testResponse("invoking stateless2", response);

            System.out.println("ensuring that sless1 and sless2 are not equal");
            if (!sless.equals(sless2)) {
                stat.addStatus("ensuring that sless1 and sless2 are not equal", stat.FAIL);
                throw new Exception("invalid equality checks on same " + "sless session beans");
            }

            response = sless3.hello();
            testResponse("invoking stateless3", response);

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("local main", stat.FAIL);
        }

        return;
    }

    private void testResponse(String testDescription, String response) {
        if (response.equals("hello")) {
            stat.addStatus(testDescription, stat.PASS);
        } else {
            stat.addStatus(testDescription, stat.FAIL);
        }
    }

}
