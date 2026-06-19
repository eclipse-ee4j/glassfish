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

package com.sun.jsftemplating.layout.descriptors.handler;

/**
 * <p>
 * This interface provides an abstraction for different locations for storing output from a handler. Implementations may
 * store values in Session, request attributes, databases, etc.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public interface OutputType {

    /**
     * <p>
     * This method is responsible for retrieving the value of the Output from the destination that was specified by handler.
     * 'key' may be null. In cases where it is not needed, it can be ignored. If it is needed, the implementation may either
     * provide a default or throw an exception.
     * </p>
     *
     * @param context The HandlerContext
     *
     * @param outDesc The IODescriptor for this Output value in which to obtain the value
     *
     * @param key The optional 'key' to use when retrieving the value from the OutputType
     *
     * @return The requested value.
     */
    Object getValue(HandlerContext context, IODescriptor outDesc, String key);

    /**
     * <p>
     * This method is responsible for setting the value of the Output to the destination that was specified by handler.
     * 'key' may be null. In cases where it is not needed, it can be ignored. If it is needed, the implementation may either
     * provide a default or throw an exception.
     * </p>
     *
     * @param context The HandlerContext
     *
     * @param outDesc The IODescriptor for this Output value in which to obtain the value
     *
     * @param key The optional 'key' to use when setting the value from the OutputType
     *
     * @param value The value to set
     */
    void setValue(HandlerContext context, IODescriptor outDesc, String key, Object value);
}
