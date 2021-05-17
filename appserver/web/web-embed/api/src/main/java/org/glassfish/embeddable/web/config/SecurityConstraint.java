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

package org.glassfish.embeddable.web.config;

import java.util.Set;

/**
 * Class used for configuring security constraints including
 * Web Resource Collections (URL patterns, HTTP methods),
 * Authorization Constraint (role names) and
 * User Data Constraints (whether the web request needs to be received
 * over a protected transport such as TLS).
 *
 * <p/> Usage example:
 *
 * <pre>
 *      SecurityConstraint securityConstraint = new SecurityConstraint();
 *      securityConstraint.setWebResourceCollection(webResources);
 *      securityConstraint.setAuthConstraint("ADMINISTRATORS");
 *      securityConstraint.setUserDataConstraint(TransportGuarantee.CONFIDENTIAL);
 * </pre>
 *
 * @see WebResourceCollection
 *
 * @author Rajiv Mordani
 * @author Amy Roh
 */

//TODO: Need to think about if we want security to be pluggable. Need to talk to Ron.
public class SecurityConstraint {

    private Set<WebResourceCollection> webResourceCollection;
    private String[] roleNames = new String[0];
    private TransportGuarantee tg;

    /**
     * Create an instance of SecurityConstraint
     */
    public SecurityConstraint() {
    }


    /**
     * Sets the web resource collection associated with this
     * security constrint
     *
     * @see WebResourceCollection
     *
     * @param webResourceCollection the web resource collection
     * for this constraint definition
     */
    public void setWebResourceCollection(Set<WebResourceCollection> webResourceCollection) {
        this.webResourceCollection = webResourceCollection;
    }

    /**
     * Gets the web resource collection for this security constraint
     *
     * @see WebResourceCollection
     *
     * @return the web resource collection for this security constraint
     */
    public Set<WebResourceCollection> getWebResourceCollection() {
        return this.webResourceCollection;
    }

    /**
     * Sets the roles authorized to access the URL patterns and HTTP methods
     *
     * @param roleNames the roles authorized to access the url patterns
     * and HTTP methods.
     */
    public void setAuthConstraint(String... roleNames) {
        this.roleNames = roleNames;
    }

    /**
     * Sets the  requirement that the constrained requests be received
     * over a protected transport layer connection. This guarantees how
     * the data will be transported between client and server. The choices
     * for type of transport guarantee include NONE, INTEGRAL, and
     * CONFIDENTIAL. If no user data constraint applies to a request, the
     * container must accept the request when received over any connection,
     * including an unprotected one.
     *
     * @see TransportGuarantee
     *
     * @param tg the transport guarntee
     */
    public void setUserDataConstraint(TransportGuarantee tg) {
        this.tg = tg;
    }

    /**
     * Gets the roles authorized to access the URL patterns and HTTP methods
     *
     * @return an array of roles as a <tt>String</tt> authorized to access
     * the URL patterns and HTTP methods.
     */
    public String[] getAuthConstraint() {
        return this.roleNames;
    }

    /**
     * Gets the transport guarantee requirements for this SecurityConstraint
     *
     * @see TransportGuarantee
     *
     * @return the transport guarantee requirement for this SecurityConstraint
     */
    public TransportGuarantee getDataConstraint() {
        return this.tg;
    }

    /**
     * Returns a formatted string of the state.
     */
    public String toString() {
        StringBuffer toStringBuffer = new StringBuffer();
        toStringBuffer.append("SecurityConstraint: ");
        toStringBuffer.append(" webResourceCollections: ").append(webResourceCollection);
        for (String roleName : roleNames) {
            toStringBuffer.append(" authorizationConstraint ").append(roleName);
        }
        toStringBuffer.append(" userDataConstraint ").append(tg);
        return toStringBuffer.toString();
    }
}
