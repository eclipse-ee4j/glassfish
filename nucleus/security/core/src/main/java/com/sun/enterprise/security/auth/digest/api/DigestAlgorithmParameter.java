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

package com.sun.enterprise.security.auth.digest.api;

import java.security.spec.AlgorithmParameterSpec;

/**
 * Interface to Digest algorithm parameters
 * @author K.Venugopal@sun.com
 */
public interface DigestAlgorithmParameter extends AlgorithmParameterSpec {
    /**
     * @returns the delimiter to be used while performing digest calculation, null otherwise.
     *
     */
     public byte[] getDelimiter();
     /**
      *
      * @returns the parameter value.
      */
     public byte[] getValue() ;
     /**
      * @returns the digest algorithm to be used.eg: MD5,MD5-sess etc..
      *
      */
     public String getAlgorithm();
     /**
      * @returns the name of the parameter, null if no name is present.
      */
     public String getName();
}
