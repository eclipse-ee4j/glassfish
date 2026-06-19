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

package com.sun.jsftemplating.util;

/**
 * <p>
 * An object that can convert a value to a different type.
 * </p>
 *
 * @author Todd Fast, todd.fast@sun.com
 * @author Mike Frisino, michael.frisino@sun.com
 */
public interface TypeConversion {

    /**
     * <p>
     * Converts the provided value to the type represented by the implementor if this interface.
     * </p>
     */
    Object convertValue(Object value);
}
