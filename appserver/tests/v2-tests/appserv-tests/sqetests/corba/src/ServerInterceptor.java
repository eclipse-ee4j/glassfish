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
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;

public class ServerInterceptor extends LocalObject implements
ServerRequestInterceptor {

    private static int nextId = 1;
    private int id = 0;
    private String name = null;

    public ServerInterceptor() {
        id = nextId++;
        name = "ServerInterceptor:" + id;
    }

    public void receive_request_service_contexts(ServerRequestInfo ri)
    throws ForwardRequest {
    }

    public void receive_request(ServerRequestInfo ri) throws ForwardRequest {
    }

    public void send_reply(ServerRequestInfo ri) {
    }

    public void send_exception(ServerRequestInfo ri) throws ForwardRequest {
    }

    public void send_other(ServerRequestInfo ri) throws ForwardRequest {
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }
}
