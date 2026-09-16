/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.orb.http.server;





import java.util.Map;

import javax.naming.NamingException;

/**
 * The seam onto the naming service.
 * <p>
 * These are the nine methods of
 * {@code com.sun.enterprise.naming.impl.SerialContextProvider}, the remote
 * interface GlassFish already publishes over IIOP for JNDI, restated without
 * {@code java.rmi} so this module stays independently compilable. The adapter
 * in the GlassFish integration module delegates straight to the existing
 * {@code TransientContext} root obtained from {@code ProviderManager}, so the
 * namespace an HTTP client sees is the same one an IIOP client sees - not a
 * copy, and not a subset.
 */
public interface NamingBridge {

    Object lookup(String name) throws NamingException;

    Object lookupLink(String name) throws NamingException;

    void bind(String name, Object value) throws NamingException;

    void rebind(String name, Object value) throws NamingException;

    void unbind(String name) throws NamingException;

    void rename(String oldName, String newName) throws NamingException;

    /**
     * @return the bindings under {@code name}. Values that are not
     *         serializable are omitted, as
     *         {@code RemoteSerialContextProviderImpl.list} already does for
     *         remote callers.
     */
    Map<String, Object> list(String name) throws NamingException;

    void createSubcontext(String name) throws NamingException;

    void destroySubcontext(String name) throws NamingException;
}
