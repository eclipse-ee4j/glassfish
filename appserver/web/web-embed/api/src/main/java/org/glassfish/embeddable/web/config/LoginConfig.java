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

/**
 * The class configures the authentication related parameters like,
 * authentication method, form login configuration, if authentication method
 * is form based authentication, the realm name and the realm type.
 *
 * <p/> Usage example:
 *
 * <pre>
 *      FormLoginConfig form = new FormLoginConfig("login.html", "error.html");
 *
 *      LoginConfig loginConfig = new LoginConfig();
 *      loginConfig.setAuthMethod(AuthMethod.FORM);
 *      loginConfig.setRealmName("userauth");
 *      loginConfig.setFormLoginConfig(form);
 * </pre>
 *
 * @see SecurityConfig
 *
 * @author Rajiv Mordani
 * @author Amy Roh
 */
public class LoginConfig {

    private AuthMethod authMethod;
    private FormLoginConfig formLoginConfig;
    private String realmName;

    public LoginConfig() {
    }

    public LoginConfig(AuthMethod authMethod, String name) {
        this.authMethod = authMethod;
        this.realmName = name;
    }

    /**
     * Set the authentication scheme to be used for a given
     * context
     *
     * @param authMethod one of the supported auth methods as
     * defined in <tt>AuthMethod</tt> enumeration
     */
    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    /**
     * Gets the auth method for the context
     * @return the authmethod for the context
     */
    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    /**
     * Sets the realm name to be used for the context
     *
     * @param realmName the realm name for the context
     */
    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    /**
     * Gets the realm name set for the context
     *
     * @return the realm name for the context
     */
    public String getRealmName() {
        return realmName;
    }

    /**
     * Set the form login configuration, if the authentication
     * method is form based authentication
     *
     * @see FormLoginConfig
     *
     * @param flc form login configuration
     */
    public void setFormLoginConfig(FormLoginConfig flc) {
        formLoginConfig = flc;
    }

    /**
     * Gets the form login config, or <tt>null</tt> if
     * the authentication scheme is not form based login.
     *
     * @see FormLoginConfig
     *
     * @return form login configuration
     */
    public FormLoginConfig getFormLoginConfig() {
        return formLoginConfig;
    }

    /**
     * Returns a formatted string of the state.
     */
    public String toString() {
        StringBuffer toStringBuffer = new StringBuffer();
        toStringBuffer.append("LoginConfig: ");
        toStringBuffer.append(" authMethod: ").append(authMethod);
        toStringBuffer.append(" formLoginConfig: ").append(formLoginConfig);
        toStringBuffer.append(" realmName ").append(realmName);
        return toStringBuffer.toString();
    }

}
