/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.enterprise.iiop.api;

import jakarta.inject.Inject;

import javax.naming.NamingException;

import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.internal.api.ORBLocator;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;

/**
 * Proxy for java:comp/ORB lookups
 *
 * @author Ken Saks
 */
@Service
@NamespacePrefixes(ORBNamingProxy.ORB_CONTEXT)
public class ORBNamingProxy implements NamedNamingObjectProxy {
    // Must not be private so it can be used in the annotation
    static final String ORB_CONTEXT = JNDI_CTX_JAVA_COMPONENT + "ORB";

    @Inject
    private ORBLocator orbLocator;

    @Override
    public Object handle(String name) throws NamingException {
        if (!ORB_CONTEXT.equals(name)) {
            return null;
        }
        try {
            return orbLocator.getORB();
        } catch(Throwable t) {
            NamingException ne = new NamingException("Error retrieving orb for java:comp/ORB lookup");
            ne.initCause(t);
            throw ne;
        }
    }
}
