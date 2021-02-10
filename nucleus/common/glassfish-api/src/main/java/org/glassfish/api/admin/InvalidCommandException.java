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

package org.glassfish.api.admin;

public class InvalidCommandException extends CommandException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates new <code>InvalidCommandException</code> without detail message.
     */
    public InvalidCommandException() {
    }

    /**
     * Constructs an <code>InvalidCommandException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidCommandException(String cmdName) {
        super(cmdName);
    }

    /**
     * Constructs a new <code>InvalidCommandException</code> exception with the specified cause.
     */
    public InvalidCommandException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new <code>InvalidCommandException</code> exception with the specified detailed message and cause.
     */
    public InvalidCommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
