/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.osgi.cli.remote.impl;


/**
 * Commands for session manipulation.
 *
 * @author David Matejcek
 */
public enum SessionOperation {

    /** Create session */
    NEW,
    /** List sessions */
    LIST,
    /** Execute command in session */
    EXECUTE,
    /** Stop session */
    STOP;

    /**
     * @param operation see {@link SessionOperation} values. Case insensitive.
     * @return {@link SessionOperation} or null if operation was null.
     * @throws IllegalArgumentException if the operation is an unknown string.
     *
     */
    public static SessionOperation parse(String operation) throws IllegalArgumentException {
        if (operation == null) {
            return null;
        }
        for (SessionOperation value : values()) {
            if (value.name().equalsIgnoreCase(operation)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported session operation: " + operation);
    }

}
