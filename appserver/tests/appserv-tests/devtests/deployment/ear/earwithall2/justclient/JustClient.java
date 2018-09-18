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

package justclient;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import justbean.JustBean;
import justbean.JustBeanHome;

public class JustClient {
    public static void main(String[] args) {
        JustClient client = new JustClient();
        client.run(args);
    }

    private void run(String[] args) {
        System.out.println("JustClient.run()... enter");

        JustBean bean = null;
        try {
            Object o = (new InitialContext()).lookup("java:comp/env/ejb/JustBean");
            JustBeanHome home = (JustBeanHome)
                PortableRemoteObject.narrow(o, JustBeanHome.class);
            bean = home.create();

            String[] marbles = bean.findAllMarbles();
            for (int i = 0; i < marbles.length; i++) {
                System.out.println(marbles[i]);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("JustClient.run()... exit");
    }

}
