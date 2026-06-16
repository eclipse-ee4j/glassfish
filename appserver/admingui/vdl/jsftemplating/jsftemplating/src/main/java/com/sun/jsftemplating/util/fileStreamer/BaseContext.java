/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.util.fileStreamer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This class provides a base implemention of {@link Context} to implement code that is likely to be needed by most
 * {@link Context} implementations.
 * </p>
 */
public abstract class BaseContext implements Context {

    /**
     * Constructor.
     */
    protected BaseContext() {
    }

    /**
     * <p>
     * This method may be used to manage arbitrary information between the code invoking the {@link FileStreamer} and the
     * <code>ContentSource</code>. This method retrieves an attribute.
     * </p>
     *
     * <p>
     * See individual {@link ContentSource} implementations for more details on supported / required attributes.
     * </p>
     */
    @Override
    public Object getAttribute(String name) {
        if (name == null) {
            return null;
        }

        // Return the value (if any)
        return _att.get(name);
    }

    /**
     * <p>
     * This provides access to all attributes in this Context.
     * </p>
     *
     * <p>
     * See individual {@link ContentSource} implementations for more details on supported / required attributes.
     * </p>
     */
    @Override
    public Set<String> getAttributeKeys() {
        return _att.keySet();
    }

    /**
     * <p>
     * This method may be used to manage arbitrary information between the code invoking the {@link FileStreamer} and the
     * <code>ContentSource</code>. This method sets an attribute.
     * </p>
     *
     * <p>
     * See individual {@link ContentSource} implementations for more details on supported / required attributes.
     * </p>
     */
    @Override
    public void setAttribute(String name, Object value) {
        if (name != null) {
            _att.put(name, value);
        }
    }

    /**
     * <p>
     * This method may be used to manage arbitrary information between the coding invoking the {@link FileStreamer} and the
     * <code>ContentSource</code>. This method removes an attribute.
     * </p>
     *
     * <p>
     * See individual {@link ContentSource} implementations for more details on supported / required attributes.
     * </p>
     */
    @Override
    public void removeAttribute(String name) {
        _att.remove(name);
    }

    /**
     * <p>
     * Application scope key for allowed paths.
     * </p>
     */
    protected static final String ALLOWED_PATHS_KEY = "__jsft_AllowPath";

    /**
     * <p>
     * Application scope key for denied paths.
     * </p>
     */
    protected static final String DENIED_PATHS_KEY = "__jsft_DenyPath";

    private Map<String, Object> _att = new HashMap<>();
}
