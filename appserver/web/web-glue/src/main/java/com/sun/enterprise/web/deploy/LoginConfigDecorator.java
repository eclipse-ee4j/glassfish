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

package com.sun.enterprise.web.deploy;

import com.sun.enterprise.deployment.web.LoginConfiguration;

import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.util.RequestUtil;

/**
 * Decorator of class <code>org.apache.catalina.deploy.LoginConfig</code>
 *
 * @author Jean-Francois Arcand
 */

public class LoginConfigDecorator extends LoginConfig {


    // ----------------------------------------------------------- Constructors

    private LoginConfiguration decoree;

    private String errorPage;

    private String loginPage;

    public LoginConfigDecorator(LoginConfiguration decoree){
        this.decoree = decoree;

        String errorPage = RequestUtil.urlDecode(decoree.getFormErrorPage());
        if (!errorPage.startsWith("/")){
            errorPage = "/" + errorPage;
        }
        setErrorPage(errorPage);

        String loginPage = RequestUtil.urlDecode(decoree.getFormLoginPage());
        if (!loginPage.startsWith("/")){
            loginPage = "/" + loginPage;
        }
        setLoginPage(loginPage);
        setAuthMethod(decoree.getAuthenticationMethod());
        setRealmName(decoree.getRealmName());
    }


}
