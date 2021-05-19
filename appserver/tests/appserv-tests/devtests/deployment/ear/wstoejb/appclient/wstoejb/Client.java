/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package wstoejb;

import javax.naming.*;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;

import wstoejb.WebServiceToEjbSEI;


/**
 * This is a appclient test
 */
public class Client {

    public static void main(String args[]) {

        boolean testPositive = (Boolean.valueOf(args[0])).booleanValue();
        try {
            Context ic = new InitialContext();

            Service myWebService = (Service)
                ic.lookup("java:comp/env/service/WstoEjbService");
            WebServiceToEjbSEI port = (WebServiceToEjbSEI) myWebService.getPort(WebServiceToEjbSEI.class);
            System.out.println(port.payload("APPCLIENT as client"));
        } catch(Throwable t) {
                        if(testPositive) {
                    t.printStackTrace();
                    System.exit(-1);
                        } else {
                                System.out.println("Recd exception as expected");
                        }
        }
                System.exit(0);
    }

}
