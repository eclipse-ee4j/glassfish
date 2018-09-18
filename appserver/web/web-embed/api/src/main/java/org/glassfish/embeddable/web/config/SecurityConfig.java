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
 * Class through which the security related parameters for a context
 * may be configured.
 *
 * @author Rajiv Mordani
 */
public class SecurityConfig {

    private Set<SecurityConstraint> securityConstraints;
    private LoginConfig lc;

    /**
     * Create an instance of SecurityConfig
     */
    public SecurityConfig() {
    }

    /**
     * Set the security constraints for a context.
     *
     * @see SecurityConstraint
     *
     * @param securityConstraints a set of constraints for the
     * context on which this security configuration applies.
     */
    public void setSecurityConstraints(Set<SecurityConstraint> securityConstraints) {
        this.securityConstraints = securityConstraints;
    }

    /**
     * Configures the login related configuration for the context
     *
     * @see LoginConfig
     *
     * @param lc the login config for the context
     */
    public void setLoginConfig(LoginConfig lc) {
        this.lc = lc;
    }

    /**
     * Gets the security constraints for the context
     *
     * @see SecurityConstraint
     *
     * @return the security constraints for the context
     */
    public Set<SecurityConstraint> getSecurityConstraints() {
        return this.securityConstraints;
    }

    /**
     * Gets the login config for the context
     *
     * @see LoginConfig
     *
     * @return the login configuration for the context
     */
    public LoginConfig getLoginConfig() {
        return this.lc;
    }

    /**
     * Returns a formatted string of the state.
     */
    public String toString() {
        StringBuffer toStringBuffer = new StringBuffer();
        toStringBuffer.append("SecurityConfig: ");
        toStringBuffer.append(" securityConstraints: ").append(securityConstraints);
        toStringBuffer.append(" loginConfig: ").append(lc);
        return toStringBuffer.toString();
    }
}
