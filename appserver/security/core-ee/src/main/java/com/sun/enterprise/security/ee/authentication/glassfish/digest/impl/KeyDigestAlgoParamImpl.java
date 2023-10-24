/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee.authentication.glassfish.digest.impl;

import com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter;
import com.sun.enterprise.security.auth.digest.api.Key;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class KeyDigestAlgoParamImpl implements DigestAlgorithmParameter, Key {

    private String userName;
    private String realmName;
    private String algorithm = null;
    private String name = "A1";
    private static byte[] delimeter = ":".getBytes();

    public KeyDigestAlgoParamImpl(String user, String realm) {
        this.userName = user;
        this.realmName = realm;
    }

    public KeyDigestAlgoParamImpl(String algorithm, String user, String realm) {
        this.userName = user;
        this.realmName = realm;
        this.algorithm = algorithm;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getRealmName() {
        return realmName;
    }

    @Override
    public byte[] getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public byte[] getDelimiter() {
        return delimeter;
    }

    @Override
    public String getName() {
        return name;
    }
}
