/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.util.stringifier;

/**
 * A registry for mapping classes to Stringifiers
 */
public interface StringifierRegistry {

    /**
     * Add a mapping from a Class to a Stringifier
     *
     * @param theClass the Class to which the Stringifier should be associated
     * @param stringifier the Stringifier for the class
     */
    void add(Class<?> theClass, Stringifier stringifier);


    /**
     * Lookup a Stringifier from a Class.
     *
     * @param theClass the Class
     * @return the Stringifier, or null if not found
     */
    Stringifier lookup(Class<?> theClass);
}
