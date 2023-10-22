/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth.login;

import com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class DigestCredentials {

    private final String realmName;
    private final String username;
    private final DigestAlgorithmParameter[] parameters;

    public DigestCredentials(String realmName, String username, DigestAlgorithmParameter[] parameters) {
        this.realmName = realmName;
        this.username = username;
        this.parameters = parameters;
    }

    public String getRealmName() {
        return realmName;
    }

    public String getUserName() {
        return username;
    }

    public DigestAlgorithmParameter[] getParameters() {
        return parameters;
    }
}
