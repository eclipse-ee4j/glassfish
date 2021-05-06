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

package com.acme;

import java.util.Properties;
import javax.naming.InitialContext;

public class Client2 {

    public static void main(String args[]) {

        String host;
         String port;

        try {
            Properties p = new Properties();
            if( args.length > 0 ) {
                host = args[0];
                p.put("org.omg.CORBA.ORBInitialHost", host);
            }

            if( args.length > 1 ) {
                port = args[1];
                p.put("org.omg.CORBA.ORBInitialPort", port);
            }

            InitialContext ic = new InitialContext(p);
            Hello h = (Hello) ic.lookup("HH#com.acme.Hello");
            h.hello();


        } catch(Exception e) {
            e.printStackTrace();
        }

    }


}
