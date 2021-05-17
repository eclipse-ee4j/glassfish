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

import java.net.URL;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Service;
import javax.xml.rpc.Call;
import javax.xml.namespace.QName;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import test.webservice.WebServiceTest;

/**
 *
 * @author dochez
 */

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        stat.addDescription("webservices-web-stubs-properties");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-web-stubs-properties");
    }

    public void doTest(String[] args) {
        try {
            dynamic(args);
            stat.addStatus("web-stubs-properties Dynamic Proxy", stat.PASS);
        } catch(Exception e) {
            System.out.println("Failure " + e.getMessage());
            e.printStackTrace();
            stat.addStatus("web-stubs-properties Dynamic Proxy", stat.FAIL);
        }
    }

    public void dynamic(String[] args) throws Exception {
        String endpoint = args[0];
        System.out.println("Invoking dynamic proxies with endpoint at " + endpoint);
        URL wsdlURL = new URL(endpoint+"?WSDL");
        ServiceFactory factory = ServiceFactory.newInstance();
        Service service = factory.createService(wsdlURL,
            new QName("urn:WebServiceTest","WebServiceServletTest"));
        System.out.println("Obtained Service");
        WebServiceTest intf = (WebServiceTest) service.getPort(
            new QName("urn:WebServiceTest","WebServiceTestPort"),
            WebServiceTest.class);
        String[] params = new String[1];
        params[0] = " from client";
        System.out.println(intf.doTest(params));
    }


}
