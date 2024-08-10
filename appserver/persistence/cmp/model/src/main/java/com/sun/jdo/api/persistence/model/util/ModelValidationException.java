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
 * ModelValidationException.java
 *
 * Created on September 22, 2000, 1:05 PM
 */

package com.sun.jdo.api.persistence.model.util;

import com.sun.jdo.api.persistence.model.ModelException;
import com.sun.jdo.spi.persistence.utility.StringHelper;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

/**
 *
 * @author raccah
 * @version %I%
 */
public class ModelValidationException extends ModelException
{
    /** Constant representing an error. */
    public static final int ERROR = 0;

    /** Constant representing a warning. */
    public static final int WARNING = 1;

    /** I18N message handler */
    private static final ResourceBundle _messages = I18NHelper.loadBundle(
        "com.sun.jdo.api.persistence.model.Bundle",        // NOI18N
        ModelValidationException.class.getClassLoader());

    /** This field holds the type -- one of {@link #ERROR} or {@link #WARNING}
     */
    private int _type;

    /** This field holds the offending object -- the one being validated
     * when the problem occurred
     */
    private Object _offendingObject;

    /** @return I18N message handler for this element
     */
    protected static final ResourceBundle getMessages ()
    {
        return _messages;
    }

    /**
     * Creates new <code>ModelValidationException</code> of type {@link #ERROR}
     * without a detail message and with <code>null</code> as the
     * offending object.
     */
    public ModelValidationException ()
    {
    }

    /**
     * Constructs a <code>ModelValidationException</code> of type
     * {@link #ERROR} with the specified detail message and
     * <code>null</code> as the offending object.
     * @param msg the detail message.
     */
    public ModelValidationException (String msg)
    {
        super(msg);
    }

    /**
     * Constructs a <code>ModelValidationException</code> of type
     * {@link #ERROR} with the specified offending object and no
     * detail message.
     * @param offendingObject the offending object.
     */
    public ModelValidationException (Object offendingObject)
    {
        super();
        _offendingObject = offendingObject;
    }

    /**
     * Constructs a <code>ModelValidationException</code> of type
     * {@link #ERROR} with the specified detail message and offending
     * object.
     * @param offendingObject the offending object.
     * @param msg the detail message.
     */
    public ModelValidationException (Object offendingObject, String msg)
    {
        this(ERROR, offendingObject, msg);
    }

    /**
     * Constructs a <code>ModelValidationException</code> of the specified
     * type  with the specified detail message and offending object.
     * @param errorType the type -- one of {@link #ERROR} or {@link #WARNING}.
     * @param offendingObject the offending object.
     * @param msg the detail message.
     */
    public ModelValidationException (int errorType, Object offendingObject,
        String msg)
    {
        super(msg);
        _type = errorType;
        _offendingObject = offendingObject;
    }

    /**
     * Get the offending object -- the one being validated when the problem
     * occurred.
     */
    public Object getOffendingObject () { return _offendingObject; }

    /**
     * Get the type -- one of {@link #ERROR} or {@link #WARNING}.
     */
    public int getType () { return _type; }

    /**
    * Returns the error message string of this throwable object.
    * @return the error message string of this
    * <code>ModelValidationException</code>, prepended with the warning string
    * if the type is {@link #WARNING}
    *
    */
    public String getMessage ()
    {
        String message = super.getMessage();

        if ((WARNING == getType()) && !StringHelper.isEmpty(message))
        {
            message    = I18NHelper.getMessage(getMessages(),
                "util.validation.warning") + message;            //NOI18N
        }

        return message;
    }
}
