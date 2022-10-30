/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ejb.spi.HandleDelegate;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import javax.naming.NamingException;

import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;

/**
 * Proxy for java:comp/ORB lookups
 *
 * @author Ken Saks
 */
@Service
@NamespacePrefixes(HandleDelegateNamingProxy.HANDLE_DELEGATE)
public class HandleDelegateNamingProxy implements NamedNamingObjectProxy {

    static final String HANDLE_DELEGATE = JNDI_CTX_JAVA_COMPONENT + "HandleDelegate";

    @Inject
    private Provider<HandleDelegateFacade> handleDelegateFacadeProvider;

    private volatile HandleDelegateFacade facade;

    @Override
    public Object handle(String name) throws NamingException {
        HandleDelegate delegate = null;

        if (HANDLE_DELEGATE.equals(name)) {
            try {
                if (facade == null) {
                    HandleDelegateFacade hd = handleDelegateFacadeProvider.get();
                    facade = hd;
                }
                delegate = facade.getHandleDelegate();

            } catch (Throwable t) {
                NamingException ne = new NamingException("Error resolving java:comp/HandleDelegate lookup");
                ne.initCause(t);
                throw ne;
            }
        }

        return delegate;
    }

}
