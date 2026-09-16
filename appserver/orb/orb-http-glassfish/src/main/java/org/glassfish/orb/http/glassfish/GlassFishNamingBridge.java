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

package org.glassfish.orb.http.glassfish;



import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.enterprise.naming.impl.ProviderManager;
import com.sun.enterprise.naming.impl.SerialContextProvider;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.glassfish.enterprise.iiop.spi.EjbContainerFacade;
import org.glassfish.orb.http.protocol.RemoteEjbReference;
import org.glassfish.orb.http.server.NamingBridge;
import org.jvnet.hk2.annotations.Service;

/**
 * The naming half of the adapter, and the part with nothing to translate.
 *
 * <p>{@code SerialContextProvider} is the remote interface GlassFish already
 * publishes over IIOP for JNDI, and its nine methods are the nine operations
 * of {@link NamingBridge}. The claim in the transport's design notes that this
 * mapping is one-to-one is not a figure of speech: every method below is a
 * delegation and nothing else.
 *
 * <p>The local provider is used rather than the remote one. The remote
 * provider exists to be reached through an ORB; we are already inside the
 * server, so going out through a remote reference to come back to the same
 * namespace would add a hop and an ORB dependency to no purpose.
 */
@Service
@Singleton
public class GlassFishNamingBridge implements NamingBridge {

    /** The portable global JNDI prefix every remote bean is published under. */
    private static final String GLOBAL = "java:global/";

    @Inject
    private EjbNameIndex index;

    private SerialContextProvider provider() {
        return ProviderManager.getProviderManager().getLocalProvider();
    }

    @Override
    public Object lookup(String name) throws NamingException {
        RemoteEjbReference bean = asEjbReference(name);
        if (bean != null) {
            return bean;
        }
        try {
            return provider().lookup(name);
        } catch (java.rmi.RemoteException e) {
            throw asNamingException("lookup", name, e);
        }
    }

    /**
     * Turns a portable global name for a remote bean into something an HTTP
     * client can use.
     *
     * <p>Without this the transport does not work at all, and the reason is
     * worth stating. What the namespace holds for a remote bean is what
     * {@code BaseContainer.publishObject} put there: an IIOP stub, or a
     * {@code Reference} naming {@code IIOPObjectFactory}. Handing either to an
     * HTTP client would be handing it a CORBA object it has no ORB to use and
     * no way to marshal.
     *
     * <p>The portable global name is itself the answer, because it already
     * carries every part of the locator:
     * {@code java:global[/<app>]/<module>/<bean>[!<interface>]}. The index is
     * consulted rather than trusted-by-syntax, so a name that parses but names
     * no deployed bean falls through to the namespace and fails there, as it
     * should.
     *
     * @param name the JNDI name being looked up
     * @return a reference the client can turn into a proxy, or null if this is
     *         not a remote bean
     */
    private RemoteEjbReference asEjbReference(String name) throws NamingException {
        if (name == null || !name.startsWith(GLOBAL)) {
            return null;
        }
        String remainder = name.substring(GLOBAL.length());

        String viewClassName = null;
        int bang = remainder.indexOf('!');
        if (bang >= 0) {
            viewClassName = remainder.substring(bang + 1);
            remainder = remainder.substring(0, bang);
        }

        String[] parts = remainder.split("/");
        String appName;
        String moduleName;
        String beanName;
        if (parts.length == 3) {
            appName = parts[0];
            moduleName = parts[1];
            beanName = parts[2];
        } else if (parts.length == 2) {
            // A standalone module: the application name is the module's.
            appName = parts[0];
            moduleName = parts[0];
            beanName = parts[1];
        } else {
            return null;
        }

        Long ejbId = index.lookup(appName, moduleName, beanName);
        if (viewClassName == null || ejbId == null) {
            // Either the client did not say which view it wants - and a bean
            // with several remote interfaces cannot be guessed at - or there is
            // no such bean deployed. Let the namespace answer.
            return null;
        }
        return new RemoteEjbReference(appName, moduleName, null, beanName, viewClassName,
                openSessionIfStateful(ejbId, viewClassName, name));
    }

    /**
     * Starts a conversation when the bean has one to start.
     *
     * <p>Looking up a stateful bean is what creates its session - two lookups
     * of the same name are two conversations, which is what an application
     * written against the EJB semantics expects and what an IIOP client gets.
     * The session is carried back inside the reference, so the proxy the client
     * builds is already addressed to its own instance.
     *
     * <p>For anything else this returns null, and the reference names the
     * single instance the container publishes.
     *
     * @param ejbId the bean being looked up
     * @param viewClassName the interface the client named
     * @param name the JNDI name, for the error if the session cannot be made
     * @return the new session's key, or null if this bean is not stateful
     * @throws NamingException if the bean is stateful and the session could
     *         not be created - a reference without one would reach the
     *         container and fail there, unrecognisably
     */
    private byte[] openSessionIfStateful(long ejbId, String viewClassName, String name)
            throws NamingException {
        if (!GlassFishContainerBridge.isStateful(ejbId)) {
            return null;
        }
        try {
            EjbContainerFacade facade = EjbContainerUtilImpl.getInstance().getContainer(ejbId);
            return facade.createSession(GlassFishContainerBridge.generatedViewName(viewClassName));
        } catch (Exception e) {
            throw asNamingException("open a session for", name, e);
        }
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        // The provider does not distinguish the two; SerialContext resolves
        // links above this level.
        return lookup(name);
    }

    @Override
    public void bind(String name, Object value) throws NamingException {
        try {
            provider().bind(name, value);
        } catch (java.rmi.RemoteException e) {
            throw asNamingException("bind", name, e);
        }
    }

    @Override
    public void rebind(String name, Object value) throws NamingException {
        try {
            provider().rebind(name, value);
        } catch (java.rmi.RemoteException e) {
            throw asNamingException("rebind", name, e);
        }
    }

    @Override
    public void unbind(String name) throws NamingException {
        try {
            provider().unbind(name);
        } catch (java.rmi.RemoteException e) {
            throw asNamingException("unbind", name, e);
        }
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        try {
            provider().rename(oldName, newName);
        } catch (java.rmi.RemoteException e) {
            throw asNamingException("rename", oldName, e);
        }
    }

    @Override
    public Map<String, Object> list(String name) throws NamingException {
        try {
            Map<String, Object> result = new HashMap<>();
            provider().list(name).forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        } catch (java.rmi.RemoteException e) {
            throw asNamingException("list", name, e);
        }
    }

    @Override
    public void createSubcontext(String name) throws NamingException {
        try {
            provider().createSubcontext(name);
        } catch (java.rmi.RemoteException e) {
            throw asNamingException("createSubcontext", name, e);
        }
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        try {
            provider().destroySubcontext(name);
        } catch (java.rmi.RemoteException e) {
            throw asNamingException("destroySubcontext", name, e);
        }
    }

    private static NamingException asNamingException(String operation, String name, Throwable cause) {
        NamingException e = new NamingException(operation + ' ' + name + " failed");
        e.initCause(cause);
        return e;
    }
}

