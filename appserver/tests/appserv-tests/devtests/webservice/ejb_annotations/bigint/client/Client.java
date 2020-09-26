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

package client;

import jakarta.xml.ws.WebServiceRef;
import java.math.BigInteger;
import endpoint.*;
import java.util.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/CustomerManagerService/CustomerManager?WSDL")
        static CustomerManagerService service;

        public static void main(String[] args) {
            stat.addDescription("webservices-bigint");
            Client client = new Client();
            client.doTest(args);
            stat.printSummary("webservices-bigint");
       }

       public void doTest(String[] args) {
            try {
                CustomerManager port = service.getCustomerManagerPort();
                Customer ret = port.createCustomer(new BigInteger("1212"), "vijay");
                boolean found = false;
                List<Object> retList = port.getCustomerList();
                Iterator it = retList.iterator();
                while(it.hasNext()) {
                    Customer c = (Customer)it.next();
                    String name = c.getName();
                    BigInteger bi = c.getBigInteger();
                    System.out.println("Name -> " + name +
                        "; BigInt = " + bi.intValue());
                    if(("vijay".equals(name)) && (bi.intValue() == 1212)) {
                        found=true;
                    }
                }
                if(!found) {
                    System.out.println("Entity not persisted as expected");
                    stat.addStatus("ejb-bigint-test", stat.FAIL);
                } else {
                    port.removeCustomer("vijay");
                    stat.addStatus("ejb-bigint-test", stat.PASS);
                }
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ejb-bigint-test", stat.FAIL);
            }
       }
}

