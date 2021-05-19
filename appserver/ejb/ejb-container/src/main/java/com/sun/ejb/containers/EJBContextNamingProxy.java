/*
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

package com.sun.ejb.containers;

import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.invocation.ComponentInvocation;

import com.sun.ejb.EjbInvocation;

import org.jvnet.hk2.annotations.Service;

import javax.naming.NamingException;

/**
 * Proxy for accessing EJBContext objects when requested by lookup or injection.
 * NamingManager will call the handle() method when the JNDI name is looked up.
 *
 *
 * @author Ken Saks
 */
@Service
@NamespacePrefixes(EJBContextNamingProxy.EJB_CONTEXT)
public class EJBContextNamingProxy
        implements NamedNamingObjectProxy {

    static final String EJB_CONTEXT
            = "java:comp/EJBContext";

    public Object handle(String name) throws NamingException {

        if (EJB_CONTEXT.equals(name)) {
            return getEJBContextObject();
        }
        return null;
    }

    private Object getEJBContextObject() {

        // Cannot store EjbContainerUtilImpl.getInstance() in an instance
        // variable because it shouldn't be accessed before EJB container
        // is initialized.
        // NamedNamingObjectProxy is initialized on the first lookup.

        ComponentInvocation currentInv =
                EjbContainerUtilImpl.getInstance().getCurrentInvocation();

        if(currentInv == null) {
            throw new IllegalStateException("no current invocation");
        } else if (currentInv.getInvocationType() !=
                   ComponentInvocation.ComponentInvocationType.EJB_INVOCATION) {
            throw new IllegalStateException
                    ("Illegal invocation type for EJB Context : "
                     + currentInv.getInvocationType());
        }

        return ((EjbInvocation) currentInv).context;
    }
}
