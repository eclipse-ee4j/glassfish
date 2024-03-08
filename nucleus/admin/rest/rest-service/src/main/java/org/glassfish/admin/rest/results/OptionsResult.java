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

package org.glassfish.admin.rest.results;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.glassfish.admin.rest.provider.MethodMetaData;

/**
 * Response information object. Returned on call to OPTIONS method. Information used by provider to generate the
 * appropriate output.
 *
 * @author Rajeshwar Patil
 */
public class OptionsResult extends Result {

    /**
     * Default Constructor
     */
    public OptionsResult() {
        this(null);
    }

    public OptionsResult(String name) {
        __name = name;
        __metaData = new HashMap<String, MethodMetaData>();
    }

    /**
     * Returns meta-data object for the given method
     */
    public MethodMetaData getMethodMetaData(String method) {
        return __metaData.get(method);
    }

    /**
     * Adds meta-data object for the given method
     */
    public MethodMetaData putMethodMetaData(String method, MethodMetaData methodMetaData) {
        return __metaData.put(method, methodMetaData);
    }

    /**
     * Returns no of method meta-data available. Should be equal to the number of methods on resource.
     */
    public int size() {
        return __metaData.size();
    }

    /**
     * Returns set of method names for which meta-data is available.
     */
    public Set<String> methods() {
        return __metaData.keySet();
    }

    Map<String, MethodMetaData> __metaData;
}
