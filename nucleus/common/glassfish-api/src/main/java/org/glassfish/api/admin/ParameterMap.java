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
}
