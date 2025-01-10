/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import java.util.List;
import java.util.Set;

import org.jvnet.hk2.component.MultiMap;

/**
 * A map from parameter name to a list of parameter values. (Really just a more convenient name for MultiMap.)
 */
public class ParameterMap extends MultiMap<String, String> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an empty ParameterMap.
     */
    public ParameterMap() {
    }

    /**
     * Copy constructor.
     */
    public ParameterMap(ParameterMap base) {
        super(base);
    }

    /**
     * Fluent API for adding parameters to the map.
     *
     * @param k
     * @param v
     * @return ParameterMap
     */
    public ParameterMap insert(String k, String v) {
        add(k, v);
        return this;
    }

    /**
     * Get a copy of this map with values of secret parameters changed into *******" to hide their values.
     * Parameters which are not secret, remain without any change.
     *
     * @param secretParameters A set of parameter names which are considered secret. Not null.
     * @return A copy of this map with masked values
     */
    public ParameterMap getMaskedMap(Set<String> secretParameters) {
        final ParameterMap maskedParameters = new ParameterMap(this);
        maskedParameters.entrySet().forEach(entry -> {
            if (secretParameters.contains(entry.getKey())) {
                entry.setValue(List.of("*******"));
            }
        });
        return maskedParameters;
    }

}
