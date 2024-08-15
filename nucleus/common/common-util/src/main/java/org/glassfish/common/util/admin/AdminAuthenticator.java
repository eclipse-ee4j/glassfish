/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.common.util.admin;

import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;

import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author tjquinn
 */
@Contract
public interface AdminAuthenticator {

    public static final String REST_TOKEN_NAME = "REST_TOKEN";
    public static final String REMOTE_ADDR_NAME = "REMOTE_ADDR";

    static enum AuthenticatorType {
            PRINCIPAL,
            REST_TOKEN,
            ADMIN_TOKEN,
            REMOTE_HOST,
            REMOTE_ADDR,
            ADMIN_INDICATOR,
            USERNAME_PASSWORD;
    }
    List<Callback> callbacks();
    boolean identify(Subject subject) throws LoginException;
    AuthenticatorType type();
}
