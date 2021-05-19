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

package com.sun.appserv.management.client.prefs;

/** An exception class to communicate the error in accessing or mutating the store.
 * @since Appserver 9.0
 */
public class StoreException extends Exception {
 static final long serialVersionUID = 8911126457304139178L;

    /**
     * Creates a new instance of <code>StoreException</code> without detail message.
     */
    public StoreException() {
    }


    /**
     * Constructs an instance of <code>StoreException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public StoreException(final String msg) {
        super(msg);
    }

    public StoreException(final Throwable t) {
        super(t);
    }

    public StoreException(final String msg, final Throwable t) {
        super (msg, t);
    }
}
