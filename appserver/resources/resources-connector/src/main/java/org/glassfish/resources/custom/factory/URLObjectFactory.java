/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.custom.factory;

import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class URLObjectFactory implements Serializable, ObjectFactory {

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
        throws Exception {
        Reference ref = (Reference) obj;
        Enumeration<RefAddr> refAddrs = ref.getAll();

        String protocol = null;
        String host = null;
        int port = -1;
        String file = null;
        String spec = null;

        while (refAddrs.hasMoreElements()) {
            RefAddr addr = refAddrs.nextElement();
            String type = addr.getType();
            String content = (String) addr.getContent();
            if (type.equalsIgnoreCase("protocol")) {
                protocol = content;
            } else if (type.equalsIgnoreCase("host")) {
                host = content;
            } else if (type.equalsIgnoreCase("port")) {
                try {
                    port = Integer.parseInt(content);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Provided port number is not a number: " + content, nfe);
                }
            } else if (type.equalsIgnoreCase("file")) {
                file = content;
            } else if (type.equalsIgnoreCase("spec")) {
                spec = content;
            }
        }

        if (protocol != null && host != null && port != -1 && file != null) {
            return new URL(protocol, host, port, file);
        } else if (protocol != null && host != null && file != null) {
            return new URL(protocol, host, file);
        } else if (spec != null) {
            return new URL(spec);
        }

        throw new IllegalArgumentException("URLObjectFactory does not have necessary parameters for URL construction");
    }
}
