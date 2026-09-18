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

package org.glassfish.orb.http.protocol;





import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * The serializable stand-in for a remote bean that a JNDI lookup returns.
 * <p>
 * A live client proxy cannot travel over the wire, and neither can an IIOP
 * stub once the ORB is out of the picture. What the naming service sends back
 * instead is this description of where the bean lives; the client turns it
 * into a proxy locally. It is the HTTP equivalent of an IOR, minus the
 * transport addressing - the address is simply the endpoint the lookup was
 * made against, which is what makes the reference work unchanged behind a load
 * balancer or after a failover.
 */
public record RemoteEjbReference(String appName,
                                 String moduleName,
                                 String distinctName,
                                 String beanName,
                                 String viewClassName,
                                 byte[] sessionId,
                                 String componentClassName) implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * A reference to a business view, which is what a client of EJB 3 and
     * later looks up.
     *
     * @param viewClassName the remote business interface
     */
    public RemoteEjbReference(String appName, String moduleName, String distinctName,
                              String beanName, String viewClassName, byte[] sessionId) {
        this(appName, moduleName, distinctName, beanName, viewClassName, sessionId, null);
    }

    /**
     * A reference to an EJB 2.x home.
     *
     * <p>The home and the component interface are two different types, and a
     * client needs both: it narrows the looked-up object to the home, calls
     * {@code create()}, and gets back the component interface. So the
     * reference has to carry both names, where a business view needs only one.
     *
     * @param homeClassName the home interface, e.g. {@code CartHome}
     * @param componentClassName the component interface, e.g. {@code Cart}
     * @return a reference a client can turn into a home proxy
     */
    public static RemoteEjbReference home(String appName, String moduleName, String distinctName,
                                          String beanName, String homeClassName,
                                          String componentClassName) {
        return new RemoteEjbReference(appName, moduleName, distinctName, beanName,
                homeClassName, null, componentClassName);
    }

    /** @return whether this names an EJB 2.x home rather than a business view */
    public boolean isHome() {
        return componentClassName != null;
    }

    public RemoteEjbReference {
        Objects.requireNonNull(appName, "appName");
        Objects.requireNonNull(moduleName, "moduleName");
        Objects.requireNonNull(beanName, "beanName");
        Objects.requireNonNull(viewClassName, "viewClassName");
        sessionId = sessionId == null ? null : sessionId.clone();
    }

    public boolean isStateful() {
        return sessionId != null;
    }

    @Override
    public byte[] sessionId() {
        return sessionId == null ? null : sessionId.clone();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RemoteEjbReference other
                && appName.equals(other.appName)
                && moduleName.equals(other.moduleName)
                && Objects.equals(distinctName, other.distinctName)
                && beanName.equals(other.beanName)
                && viewClassName.equals(other.viewClassName)
                && Arrays.equals(sessionId, other.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, moduleName, distinctName, beanName, viewClassName)
                * 31 + Arrays.hashCode(sessionId);
    }

    @Override
    public String toString() {
        return "RemoteEjbReference[" + viewClassName + " @ " + appName + '/' + moduleName
                + '/' + (distinctName == null ? "-" : distinctName) + '/' + beanName + ']';
    }
}
