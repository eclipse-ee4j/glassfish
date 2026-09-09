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


import org.glassfish.orb.http.protocol.EjbKey;

/**
 * The seam onto the EJB container.
 * <p>
 * This mirrors, method for method, what GlassFish already exposes to its IIOP
 * transport in {@code org.glassfish.enterprise.iiop.spi.EjbContainerFacade}:
 *
 * <pre>
 * Remote getTargetObject(byte[] instanceKey, String generatedRemoteBusinessIntf);
 * void   releaseTargetObject(Remote remoteObj);
 * ClassLoader getClassLoader();
 * </pre>
 *
 * whose implementation in {@code BaseContainer} carries the comment
 * <em>"Called from the ProtocolManager when a remote invocation arrives."</em>
 * That is precisely the contract an HTTP endpoint needs, and it needs no
 * change to satisfy it - which is the single most important reason this
 * transport is a small piece of work rather than a large one.
 * <p>
 * It is restated here, free of {@code java.rmi} and of GlassFish types, so
 * that this module stays independently compilable and testable. The adapter
 * that binds it to the real container lives in the GlassFish integration
 * module.
 */
public interface ContainerBridge {

    /** Thrown when a bean, view or session does not exist. */
    class NoSuchTargetException extends Exception {

        private static final long serialVersionUID = 1L;

        public NoSuchTargetException(String message) {
            super(message);
        }
    }

    /**
     * Resolves the four-part bean name, plus an optional stateful session id,
     * into the container's object key.
     *
     * @param sessionId the stateful session, or {@code null} for the home /
     *                  a stateless bean
     */
    EjbKey resolve(String appName,
                   String moduleName,
                   String distinctName,
                   String beanName,
                   byte[] sessionId) throws NoSuchTargetException;

    /**
     * The target to invoke on.
     *
     * @param viewClassName the remote business interface, or {@code null} for
     *                      the remote home view
     * @see #releaseTargetObject(Object)
     */
    Object getTargetObject(EjbKey key, String viewClassName) throws NoSuchTargetException;

    /**
     * Releases a target obtained from {@link #getTargetObject}. Always called,
     * including after a failure: in GlassFish this is what pops the context
     * class loader that {@code externalPreInvoke} pushed.
     */
    void releaseTargetObject(Object target);

    /**
     * The deployment's class loader. Used to resolve parameter types and to
     * deserialize arguments - resolving them through this module's own loader
     * would find the container's classes, not the application's.
     */
    ClassLoader classLoader(EjbKey key);

    /** Creates a stateful session and returns its id. */
    byte[] createSession(String appName,
                         String moduleName,
                         String distinctName,
                         String beanName) throws NoSuchTargetException;
}
