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
 * ModelVetoException.java
 *
 * Created on August 23, 2000, 10:50 PM
 */

package com.sun.jdo.api.persistence.model;

import com.sun.jdo.spi.persistence.utility.StringHelper;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 *
 * @author raccah
 * @version %I%
 */
public class ModelVetoException extends ModelException
{
    /** This field holds the target if the
     * ModelVetoException (Throwable target) constructor was
     * used to instantiate the object
     */
    private Throwable _target;

    /**
     * Creates new <code>ModelVetoException</code> without detail message and
     * <code>null</code> as the target exception.
     */
    public ModelVetoException ()
    {
    }

    /**
     * Constructs an <code>ModelVetoException</code> with the specified
     * detail message and <code>null</code> as the target exception..
     * @param msg the detail message.
     */
    public ModelVetoException (String msg)
    {
        super(msg);
    }

    /**
     * Constructs a ModelVetoException with a target exception.
     */
    public ModelVetoException (Throwable target)
    {
        super();
        _target = target;
    }

    /**
     * Constructs a ModelVetoException with a target exception
     * and a detail message.
     */
    public ModelVetoException (Throwable target, String s)
    {
        super(s);
        _target = target;
    }

    /**
     * Get the thrown target exception.
     */
    public Throwable getTargetException() { return _target; }

    /**
    * Returns the error message string of this throwable object.
    * @return the error message string of this <code>ModelVetoException</code>
    * object if it was created with an error message string, the error
    * message of the target exception if it was not created a message
    * but the target exception has a message, or <code>null</code> if
    * neither has an error message.
    *
    */
    public String getMessage()
    {
        String message = super.getMessage();

        if (StringHelper.isEmpty(message))
        {
            Throwable target = getTargetException();

            message    = target.getMessage();
        }

        return message;
    }

    /**
     * Prints the stack trace of the thrown target exception.
     * @see java.lang.System#err
     */
    public void printStackTrace ()
    {
        printStackTrace(System.err);
    }

    /**
     * Prints the stack trace of the thrown target exception to the specified
     * print stream.
     */
    public void printStackTrace (PrintStream ps)
    {
        synchronized (ps)
        {
            Throwable target = getTargetException();

            if (target != null)
            {
                ps.print(getClass() + ": ");            // NOI18N
                target.printStackTrace(ps);
            }
            else
                super.printStackTrace(ps);
        }
    }

    /**
     * Prints the stack trace of the thrown target exception to the
     * specified print writer.
     */
    public void printStackTrace (PrintWriter pw)
    {
        synchronized (pw)
        {
            Throwable target = getTargetException();

            if (target != null)
            {
                pw.print(getClass() + ": ");            // NOI18N
                target.printStackTrace(pw);
            }
            else
                super.printStackTrace(pw);
        }
    }
}
