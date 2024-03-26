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

/**
 * Implementation class for Digest algorithm parameters.
 *
 * @author K.Venugopal@sun.com
 */

public class DigestAlgorithmParameterImpl implements com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter {

    private byte[] delimiter = ":".getBytes();
    private String algorithm = "";
    private byte[] data = null;
    private String name = "";

    public DigestAlgorithmParameterImpl(String name, byte[] data) {
        this.data = data;
        this.name = name;
    }

    public DigestAlgorithmParameterImpl(String name, byte[] data, byte delimiter) {
        this.data = data;
        this.delimiter = new byte[] { delimiter };
        this.name = name;
    }

    public DigestAlgorithmParameterImpl(String name, String algorithm, byte[] data) {
        this.algorithm = algorithm;
        this.data = data;
        this.name = name;
    }

    public DigestAlgorithmParameterImpl(String name, String algorithm, byte[] data, byte[] delimiter) {
        this.algorithm = algorithm;
        this.data = data;
        this.delimiter = delimiter;
        this.name = name;
    }

    @Override
    public String getAlgorithm() {
        return this.algorithm;
    }

    @Override
    public byte[] getValue() {
        return data;
    }

    @Override
    public byte[] getDelimiter() {
        return delimiter;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
