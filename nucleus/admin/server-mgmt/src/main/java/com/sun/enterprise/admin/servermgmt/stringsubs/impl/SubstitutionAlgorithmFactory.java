/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import com.sun.enterprise.admin.servermgmt.stringsubs.SubstitutionAlgorithm;
import com.sun.enterprise.admin.servermgmt.stringsubs.impl.algorithm.RadixTreeSubstitutionAlgo;

import java.util.Map;

/**
 * A factory to retrieve the algorithm used to perform String substitution.
 */
public class SubstitutionAlgorithmFactory {

    /**
     * Get's the algorithm use to perform string substitution.
     *
     * @param substitutionMap {@link Map} of substitutable key/value pairs.
     * @return String substitution algorithm.
     */
    public SubstitutionAlgorithm getAlgorithm(Map<String, String> substitutionMap) {
        return new RadixTreeSubstitutionAlgo(substitutionMap);
    }
}
