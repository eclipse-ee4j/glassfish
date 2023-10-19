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

import java.security.spec.AlgorithmParameterSpec;

import com.sun.enterprise.security.auth.digest.api.NestedDigestAlgoParam;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class NestedDigestAlgoParamImpl implements NestedDigestAlgoParam {

    private byte[] delimeter = ":".getBytes();
    private String algorithm = "";
    private AlgorithmParameterSpec[] params = null;
    private String name = "";

    public NestedDigestAlgoParamImpl(String algorithm, String name, AlgorithmParameterSpec[] values) {
        this.algorithm = algorithm;
        this.params = values;
        this.name = name;
    }

    public NestedDigestAlgoParamImpl(String name, AlgorithmParameterSpec[] values) {
        this.params = values;
        this.name = name;
    }

    public NestedDigestAlgoParamImpl(String algorithm, String name, AlgorithmParameterSpec[] values, byte[] delimiter) {
        this.algorithm = algorithm;
        this.params = values;
        this.delimeter = delimiter;
        this.name = name;
    }

    public NestedDigestAlgoParamImpl(AlgorithmParameterSpec[] values, String name, byte[] delimiter) {
        this.params = values;
        this.delimeter = delimiter;
        this.name = name;
    }

    @Override
    public AlgorithmParameterSpec[] getNestedParams() {
        return params;
    }

    @Override
    public byte[] getDelimiter() {
        return delimeter;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public byte[] getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return name;
    }
}
