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

import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;

/**
 * This Factory provides DigestParameterGenerator instances to generate DigestAlgorithmParameter objects from Http and
 * Sip servlet requests.
 *
 * @author K.Venugopal@sun.com
 */
public abstract class DigestParameterGenerator {

    public static final String HTTP_DIGEST = "HttpDigest";
    public static final String SIP_DIGEST = "SIPDigest";

    public DigestParameterGenerator() {
    }

    // TODO: Ability to return implementations through services mechanism.
    public static DigestParameterGenerator getInstance(String algorithm) {
        if (HTTP_DIGEST.equals(algorithm)) {
        }
        return new HttpDigestParamGenerator();
    }

    public abstract DigestAlgorithmParameter[] generateParameters(AlgorithmParameterSpec value) throws InvalidAlgorithmParameterException;
}
