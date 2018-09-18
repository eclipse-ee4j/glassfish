/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package corba;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

public class ORBInitializerImpl extends LocalObject implements ORBInitializer {

    public static boolean server = true;

    public void pre_init(ORBInitInfo info) {
        System.out.println("ORBInitializer pre_int()");

        int count = 3;
        try {
            if (server) {
                for (int i = 0; i < count; i++) {
                    ServerInterceptor sl = new ServerInterceptor();
                    info.add_server_request_interceptor(sl);
                    System.out.println("ServerInterceptor " + (i + 1) +
                                       " registered");
                }
                server = false;
            } else {
                for (int i = 0; i < count; i++) {
                    ClientInterceptor cl = new ClientInterceptor();
                    info.add_client_request_interceptor(cl);
                    System.out.println("ClientInterceptor " + (i + 1) +
                                       " registered");
                }
            }
        } catch (DuplicateName e) {
            e.printStackTrace();
        }
    }

    public void post_init(ORBInitInfo info) {
        System.out.println("ORBInitializer post_init()");
    }
}

