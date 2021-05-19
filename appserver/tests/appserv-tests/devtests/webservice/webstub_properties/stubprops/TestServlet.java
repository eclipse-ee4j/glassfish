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

/*
 * TestServlet.java
 *
 * Created on September 13, 2004, 3:16 PM
 */

package stubprops;

import test.webservice.WebServiceTest;
import java.rmi.RemoteException;
import javax.naming.InitialContext;
import javax.xml.rpc.Service;


/**
 *
 * @author dochez
 */
public class TestServlet implements WebServiceTest {

    SimpleServer port;

    /** Creates a new instance of TestServlet */
    public TestServlet() {
        System.out.println("Test servlet instantiated");
    }

    public String doTest(String[] parameters) throws RemoteException {

        System.out.println("Test servlet invoked");
        Service ref;
        try {
            InitialContext ic = new InitialContext();
            ref = (Service) ic.lookup("java:comp/env/service/SimpleServiceReference");
        } catch(javax.naming.NamingException e) {
            e.printStackTrace();
            return "Failed - " + e.getMessage();
        }
        if (ref==null) {
            System.out.println("failure : cannot get service ref");
            return "Failed - Cannot get ref";
        }
        try {
        java.util.Iterator itr = ref.getPorts();
        while (itr.hasNext()) {
            System.out.println(itr.next());

        }

            port = (SimpleServer) ref.getPort(SimpleServer.class);
        } catch(javax.xml.rpc.ServiceException e) {
            e.printStackTrace();
            System.out.println("Failed - Cannot get port");
            return "Failed - " + e.getMessage();
        }
        if (port==null) {
            System.out.println("failure : cannot get port");
            return "Failed - Cannot get port";
        }
        return port.sayHello(parameters[0]);
    }
}
