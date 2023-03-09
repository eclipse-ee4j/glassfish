/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
 * Specifies the information about authentication.
 *
 * @author Danny Coward
 */
public interface LoginConfiguration {

    String BASIC_AUTHENTICATION = "BASIC";
    String FORM_AUTHENTICATION = "FORM";
    String DIGEST_AUTHENTICATION = "DIGEST";
    String CLIENT_CERTIFICATION_AUTHENTICATION = "CLIENT-CERT";

    String getAuthenticationMethod();

    void setAuthenticationMethod(String authorizationMethod);

    String getRealmName();

    void setRealmName(String relamName);

    String getFormLoginPage();

    void setFormLoginPage(String formLoginPage);

    String getFormErrorPage();

    void setFormErrorPage(String formErrorPage);

}
