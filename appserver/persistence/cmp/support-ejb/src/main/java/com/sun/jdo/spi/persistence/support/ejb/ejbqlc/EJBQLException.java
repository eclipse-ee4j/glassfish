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

/*
 * EJBQLException.java
 *
 * Created on November 12, 2001
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbqlc;

/**
 * This class represents errors reported by the EJBQL compiler.
 *
 * @author  Michael Bouschen
 */
public class EJBQLException
    extends RuntimeException
{
    /** The Throwable that caused this EJBQLException. */
    Throwable cause;

    /**
     * Creates a new <code>EJBQLException</code> without detail message.
     */
    public EJBQLException()
    {
    }

    /**
     * Constructs a new <code>EJBQLException</code> with the specified
     * detail message.
     * @param msg the detail message.
     */
    public EJBQLException(String msg)
    {
        super(msg);
    }

    /**
      * Constructs a new <code>EJBQLException</code> with the specified
      * detail message and cause.
      * @param msg the detail message.
      * @param cause the cause <code>Throwable</code>.
      */
    public EJBQLException(String msg, Throwable cause)
    {
        super(msg);
        this.cause = cause;
    }

    /**
     * Returns the cause of this <code>EJBQLException</code> or
     * <code>null</code> if the cause is nonexistent or unknown.
     * @return the cause of this or <code>null</code> if the
     * cause is nonexistent or unknown.
     */
    public Throwable getCause()
    {
        return cause;
    }

    /**
     * The <code>String</code> representation includes the name of the class,
     * the descriptive comment (if any),
     * and the <code>String</code> representation of the cause
     * <code>Throwable</code> (if any).
     * @return the <code>String</code>.
     */
    public String toString() {
        // calculate approximate size of the String to return
        StringBuffer sb = new StringBuffer();
        sb.append (super.toString());
        // include cause Throwable information
        if (cause != null) {
            sb.append("\n");  //NOI18N
            sb.append("Nested exception"); //NOI18N
            sb.append("\n");  //NOI18N
            sb.append(cause.toString());
        }
        return sb.toString();
    }
}
