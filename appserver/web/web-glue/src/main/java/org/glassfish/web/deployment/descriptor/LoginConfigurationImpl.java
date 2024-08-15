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

package org.glassfish.web.deployment.descriptor;


import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
//END OF IASRI 4660482

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.web.LogFacade;


/**
 * I dictate how the web app I belong to should be logged into.
 * @author Danny Coward
 */
public class LoginConfigurationImpl extends Descriptor implements LoginConfiguration {

    static final Logger _logger = LogFacade.getLogger();

    /** teh client authenticates using http basic authentication. */
    public static final String AUTHENTICATION_METHOD_BASIC = LoginConfiguration.BASIC_AUTHENTICATION;
    /** Digest authentication. */
    public static final String AUTHENTICATION_METHOD_DIGEST = LoginConfiguration.DIGEST_AUTHENTICATION;
    /** FOrm authentication. */
    public static final String AUTHENTICATION_METHOD_FORM = LoginConfiguration.FORM_AUTHENTICATION;
    /** The client sends a certificate. */
    public static final String AUTHENTICATION_METHOD_CLIENT_CERTIFICATE = LoginConfiguration.CLIENT_CERTIFICATION_AUTHENTICATION;

    private String authenticationMethod;
    private String realmName = "";
    private String formLoginPage = "";
    private String formErrorPage = "";
    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(LoginConfigurationImpl.class);

    /** Return my authentication method. */
    @Override
    public String getAuthenticationMethod() {
        if (this.authenticationMethod == null) {
            // START OF IASRI 4660482 - warning log if authentication method isn't defined in
            // descriptor
            _logger.log(Level.WARNING, LogFacade.AUTH_METHOD_NOT_FOUND);
            // END OF IASRI 4660482
            this.authenticationMethod = AUTHENTICATION_METHOD_BASIC;
        }
        return this.authenticationMethod;
    }

    /** Sets my authentication method. */
    @Override
    public void setAuthenticationMethod(String authenticationMethod) {

        if (Descriptor.isBoundsChecking()) {

            if (!LoginConfiguration.BASIC_AUTHENTICATION.equals(authenticationMethod)
                && !LoginConfiguration.DIGEST_AUTHENTICATION.equals(authenticationMethod)
                && !LoginConfiguration.FORM_AUTHENTICATION.equals(authenticationMethod)
                && !LoginConfiguration.CLIENT_CERTIFICATION_AUTHENTICATION.equals(authenticationMethod) ) {

                throw new IllegalArgumentException(
                    MessageFormat.format(
                        _logger.getResourceBundle().getString(LogFacade.EXCEPTION_AUTH_METHOD),
                        authenticationMethod));

            }
        }
        this.authenticationMethod = authenticationMethod;

    }


    /** Obtain the realm the server should use for basic authentication. */
    @Override
    public String getRealmName() {
        if (this.realmName == null) {
            this.realmName = "";
        }
        return this.realmName;
    }


    /** Set the realm the server should use for basic authentication. */
    @Override
    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }


    /** Get the name of the login page for form login. */
    @Override
    public String getFormLoginPage() {
        if (this.formLoginPage == null) {
            this.formLoginPage = "";
        }
        return this.formLoginPage;
    }


    /** Set the name of the login page for form login. */
    @Override
    public void setFormLoginPage(String formLoginPage) {
        this.formLoginPage = formLoginPage;
    }


    /** Get the name of the error page for form login. */
    @Override
    public String getFormErrorPage() {
        if (this.formErrorPage == null) {
            this.formErrorPage = "";
        }
        return this.formErrorPage;
    }


    /** Set the name of the error page for form login. */
    @Override
    public void setFormErrorPage(String formErrorPage) {
        this.formErrorPage = formErrorPage;
    }

    /** My representation as a formatted String.*/
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("LoginConfig:(").append(authenticationMethod).append(" ").append(
            realmName).append(" ").append(formLoginPage).append(" ").append(formErrorPage).append(")");
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof LoginConfigurationImpl) {
            LoginConfigurationImpl other = (LoginConfigurationImpl)obj;
            result = getAuthenticationMethod().equals(other.getAuthenticationMethod()) &&
                getRealmName().equals(other.getRealmName()) &&
                getFormLoginPage().equals(other.getFormLoginPage()) &&
                getFormErrorPage().equals(other.getFormErrorPage());
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + getAuthenticationMethod().hashCode();

        if (getRealmName().length() > 0) {
            hashCode = 31 * hashCode + getRealmName().hashCode();
        }
        if (getFormLoginPage().length() > 0) {
            hashCode = 31 * hashCode + getFormLoginPage().hashCode();
        }
        if (getFormErrorPage().length() > 0) {
            hashCode = 31 * hashCode + getFormErrorPage().hashCode();
        }
        return hashCode;
    }
}
