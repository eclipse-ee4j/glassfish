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
 * Class that is used for configuring form based login, when
 * the authentication method is set to <tt>FORM</tt> in <tt>LoginConfig</tt>.
 *
 * @see LoginConfig
 * @see AuthMethod
 *
 * @author Rajiv Mordani
 * @author Amy Roh
 */
public class FormLoginConfig {

    private String loginPage;
    private String errorPage;

    /**
     * Creates an instance of the <tt>FormLoginConfig</tt> with the specified <tt>loginPage</tt> and
     * <tt>errorPage</tt>
     *
     * @param loginPage the login page
     * @param errorPage the form error page
     */
    public FormLoginConfig(String loginPage, String errorPage) {
        this.loginPage = loginPage;
        this.errorPage = errorPage;
    }

    /**
     * Gets the login page
     *
     * @return the login page for form based authentication as a <tt>String</tt>
     */
    public String getFormLoginPage() {
        return this.loginPage;
    }

    /**
     * Get the form error page
     *
     * @return the error page for form based authentication as a <tt>String</tt>
     */
    public String getFormErrorPage() {
        return this.errorPage;
    }

    /**
     * Returns a formatted string of the state.
     */
    public String toString() {
        StringBuffer toStringBuffer = new StringBuffer();
        toStringBuffer.append("FormLoginConfig: ");
        toStringBuffer.append(" loginPage: ").append(loginPage);
        toStringBuffer.append(" errorPage: ").append(errorPage);
        return toStringBuffer.toString();
    }

}
