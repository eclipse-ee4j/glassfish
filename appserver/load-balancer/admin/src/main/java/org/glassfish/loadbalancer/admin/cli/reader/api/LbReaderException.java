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

package org.glassfish.loadbalancer.admin.cli.reader.api;

/**
 * An exception that provides information on reader error.
 *
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public class LbReaderException extends Exception {

    /**
     * Constructs a reader exception with the specified message
     * and cause.
     *
     * @param   msg    the detail message for this exception
     * @param   cause  the cause of this error
     */
    public LbReaderException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a reader exception with the cause.
     *
     * @param   cause  the cause of this error
     */
    public LbReaderException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a reader exception with the specified message.
     *
     * @param   msg    the detail message for this exception
     */
    public LbReaderException(String msg) {
        super(msg);
    }

    /**
     * Constructs a reader exception.
     */
    public LbReaderException() {
        super();
    }
}
