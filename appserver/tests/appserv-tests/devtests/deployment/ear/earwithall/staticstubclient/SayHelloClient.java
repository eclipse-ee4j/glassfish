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

package staticstubclient;

import javax.xml.rpc.Stub;
import helloservice.*;

public class SayHelloClient {

    private String endpointAddress;

    public static void main(String[] args) {

        System.out.println("Endpoint address = " + args[0]);
        boolean testPositive = (Boolean.valueOf(args[1])).booleanValue();
        try {
            Stub stub = createProxy();
            stub._setProperty
              (javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY,
               args[0]);
                        SayHello hello = (SayHello)stub;
            System.out.println(hello.sayHello("Jerome !"));
        } catch (Exception ex) {
            if(testPositive) {
                ex.printStackTrace();
                System.exit(-1);
            } else {
                System.out.println("Exception recd as expected");
            }
        }
        System.exit(0);
    }

    private static Stub createProxy() {
        // Note: MyHelloService_Impl is implementation-specific.
        return
        (Stub) (new SayHelloService_Impl().getSayHelloPort());
    }
}
