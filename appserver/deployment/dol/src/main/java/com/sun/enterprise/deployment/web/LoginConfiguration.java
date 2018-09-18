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

package com.sun.enterprise.deployment.web;

/**
    * @author Danny Coward
     */

public interface LoginConfiguration {
    public static String BASIC_AUTHENTICATION = "BASIC";
    public static String FORM_AUTHENTICATION = "FORM";
    public static String DIGEST_AUTHENTICATION = "DIGEST";
    public static String CLIENT_CERTIFICATION_AUTHENTICATION = "CLIENT-CERT";


    public String getAuthenticationMethod();
    public void setAuthenticationMethod(String authorizationMethod);
    public String getRealmName();
    public void setRealmName(String relamName);
    public String getFormLoginPage();
    public void setFormLoginPage(String formLoginPage);
    public String getFormErrorPage();
    public void setFormErrorPage(String formErrorPage);

}


